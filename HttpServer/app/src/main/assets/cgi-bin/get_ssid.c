#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <linux/input.h>
#include <time.h>
#include <sys/time.h>
#include <sys/select.h>

#include <netinet/in.h>
#include <sys/socket.h>
#include <linux/netlink.h>
#include <linux/ioctl.h>
#include <net/if.h>

#include "checkservice.h"

typedef struct txtnode
{
    struct txtnode* next;
    int level;
    char *SSIDname;
    char *channel;
    int IETitle;
} TXTNODE;

TXTNODE *Head = NULL;


void insert_for_sort_operation(TXTNODE** ppNode, TXTNODE* pNode)
{
    TXTNODE *prev;
    TXTNODE *cur;

    if(pNode->level > (*ppNode)->level)
    {
        pNode->next = *ppNode;
        *ppNode = pNode;
        return;
    }

    cur = *ppNode;
    while(cur)
    {
        if(pNode->level > cur->level)
        {
            break;
        }

        prev = cur;
        cur = cur->next;
    }

    pNode->next = prev->next;
    prev->next = pNode;

    return;
}


void sort_for_link_node(TXTNODE** ppNode)
{
    TXTNODE* prev;
    TXTNODE* curr;

    if(NULL == ppNode || NULL == *ppNode)
    {
        return;
    }

    curr = (*ppNode)->next;
    (*ppNode)->next = NULL;

    while(curr)
    {
        prev = curr;
        curr = curr->next;
        insert_for_sort_operation(ppNode, prev);
    }

    return;
}


void delete_link(TXTNODE**Head)
{
	TXTNODE *p0,*p1;
	p1 = *Head;
	while(p1)
	{
		p0 = p1;
		p1=p1->next;
		free(p0->SSIDname);
		free(p0->channel);
		free(p0);
	}

	*Head = NULL;

    return;
}

TXTNODE *read_ap_list(FILE *rfp, int *count)
{
	char buf[10240];

	TXTNODE *head = NULL;
    TXTNODE *p = NULL;
    TXTNODE *tmp = NULL;
	int node_num = 0;

    memset(buf, 0, sizeof(buf));
	while (!feof(rfp))
	{
		fgets(buf,10240, rfp);
		if(!*buf)
        {
            continue;
        }

		buf[strlen(buf)] = '\0';
		if(strstr(buf, "Cell"))
		{
			node_num++;
			tmp=(TXTNODE*)malloc(sizeof(TXTNODE));
			memset(tmp,0,sizeof(TXTNODE));
    		if(head==NULL)
    		{
    			head=tmp;
    		}
    		else
    		{
    			p->next=tmp;
    		}
			p = tmp;
			tmp->next = NULL;
		}

		if(tmp != NULL && strstr(buf, "Encryption key:off"))
		{
			tmp->IETitle = 0;
		}

		if(tmp != NULL && strstr(buf, "Encryption key:on"))
		{
			tmp->IETitle = 1;

		}

		if(tmp != NULL && strstr(buf, "IE: WPA") )
		{
			tmp->IETitle = 2;
		}

        if (tmp != NULL && strstr(buf, "IE: IEEE") && strstr(buf, "WPA2"))
        {
			tmp->IETitle = 3;
        }

		if(tmp != NULL && strstr(buf, "ESSID:"))
		{
			 tmp->SSIDname = get_select_str(buf, '"', '"');
		}

		if(tmp != NULL && strstr(buf, "Channel"))
		{
			tmp->channel = get_select_str(strstr(buf, "Channel"),' ',')');
		}

		if(tmp != NULL && strstr(buf, "level="))
		{
			tmp->level = atoi(get_select_str(strstr(buf, "level="),'=','/'));
		}
	}

	*count = node_num;
	return head;
}

int main(void)
{
	system("iwlist wlan1 scanning > /tmp/iwlist");
	
	FILE *rfp;
	rfp = fopen("/tmp/iwlist","rb");
	if(rfp == NULL)
	{
		return 1;
	}

	int count = 0;
	Head = read_ap_list(rfp, &count);
	sort_for_link_node(&Head);
	fclose(rfp);

    printf("{\n");
	printf("\"respCode\" : \"0\",\n");
    printf("\"count\" : %d,\n", count);
	int is_first = 1;

	TXTNODE *p0 = Head;
	while (p0)
	{
		if (strlen(p0->SSIDname) <=0)
		{
			p0 = p0->next;
			continue;
		}
		if (is_first)
		{
			printf("\"ssid\": [\n");
			is_first = 0;
		}

		printf("{\n");

		printf("\"ssid_name\" : \"%s\",\n", p0->SSIDname);
		printf("\"ssid_type\" : %d\n", p0->IETitle);

		p0 = p0->next;

		if (NULL != p0)
		{
			printf("},\n");
		}
		else
		{
			printf("}\n");
			printf("]\n");
		}
	}

	printf("}\n");

	delete_link(&Head);
	return 0;
}


