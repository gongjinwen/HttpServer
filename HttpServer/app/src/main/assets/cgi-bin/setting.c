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

static int get_iface_addr(char *intface, char *ipAddr)
{
    int sock;
    unsigned int ip;
    struct ifreq ifr;

    if (NULL == intface || NULL == ipAddr)
    {
        return -1;
    }

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        return -2;
    }

    strcpy(ifr.ifr_name, intface);
    ifr.ifr_addr.sa_family = AF_INET;

    if (ioctl(sock, SIOCGIFADDR, &ifr) < 0)
    {
        close(sock);
        return -3;
    }

    ip = (unsigned int)(((struct sockaddr_in *)&ifr.ifr_addr)->sin_addr.s_addr);
    ip = ntohl(ip);

    sprintf(ipAddr, "%d.%d.%d.%d",
            (ip >> 24) & 0xFF, (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF, ip & 0xFF);

    close(sock);

    return 0;
}


static int get_conn_ssid(char *ssid, int max_len)
{
	if (NULL == ssid)
	{
		return 0;
	}

	char buf[512] = {0};
	FILE *fp = NULL;
	char *tmp_pos = NULL;
	char *end_pos = NULL;
	int tmp_len = 0;
	if((fp = popen("iwgetid  wlan0", "r")) == NULL)
	{
		return -1;
	}

    char ip_addr[64];
    if (0 != get_iface_addr("wlan0", ip_addr))
    {
        return 0;
    }

	if (fgets(buf, 512, fp) != NULL)
	{
		tmp_pos = strstr(buf, "ESSID:");
		if (NULL != tmp_pos)
		{
		    tmp_pos += 6; //skip ESSID
			tmp_pos++;   //skip "
		    end_pos = buf + strlen(buf);
			tmp_len = end_pos - tmp_pos;
            tmp_len--;   //skip \n
			tmp_len--;   //skip "

			if (tmp_len > max_len)
			{
				tmp_len = max_len;
			}
			memcpy(ssid, tmp_pos, tmp_len);
			ssid[tmp_len] = '\0';
			pclose(fp);

			return 1;
		}
	}

	pclose(fp);

	return 0;
}

static int get_ap_ssid(char *ssid, int max_len)
{
	if (NULL == ssid)
	{
		return 0;
	}

	char buf[512] = {0};
	FILE *fp = NULL;
	char *tmp_pos = NULL;
	char *end_pos = NULL;
	int tmp_len = 0;
	if((fp = popen("iwgetid  wlan1", "r")) == NULL)
	{
		return -1;
	}

    char ip_addr[64];
    if (0 != get_iface_addr("wlan1", ip_addr))
    {
        return 0;
    }

	if (fgets(buf, 512, fp) != NULL)
	{
		tmp_pos = strstr(buf, "ESSID:");
		if (NULL != tmp_pos)
		{
		    tmp_pos += 6; //skip ESSID
			tmp_pos++;   //skip "
		    end_pos = buf + strlen(buf);
			tmp_len = end_pos - tmp_pos;
            tmp_len--;   //skip \n
			tmp_len--;   //skip "

			if (tmp_len > max_len)
			{
				tmp_len = max_len;
			}
			memcpy(ssid, tmp_pos, tmp_len);
			ssid[tmp_len] = '\0';
			pclose(fp);

			return 1;
		}
	}

	pclose(fp);
	return 0;
}

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


TXTNODE *read_ap_list(FILE *rfp)
{
	char buf[10240];

	TXTNODE *head = NULL;
    TXTNODE *p = NULL;
    TXTNODE *tmp = NULL;

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

	return head;
}

void write_ap_info(TXTNODE *Head)
{
	TXTNODE *p0 = Head;
	FILE *wfp = NULL;
    int has_conn = 0;

    char ssid_value[128];
    has_conn = get_conn_ssid(ssid_value, 127);

    while (p0)
	{
        if (strlen(p0->SSIDname) <=0)
        {
            p0 = p0->next;
            continue;
        }

        if (0 == p0->IETitle)  //open
        {
            printf("<option value=\"%s&ap_type=%d\">%s</option>\n",
                p0->SSIDname, p0->IETitle, p0->SSIDname);
        }
        else
        {
            printf("<option value=\"%s&ap_type=%d\" data-password>%s</option>\n",
                p0->SSIDname, p0->IETitle, p0->SSIDname);
        }

		p0 = p0->next;
	}

	if(wfp != NULL)
	{
		fclose(wfp);
	}

    return;
}


int get_wifi_list(void)
{
	system("iwlist wlan1 scanning > /tmp/iwlist");

    FILE *rfp;
    rfp = fopen("/tmp/iwlist","rb");
	if(rfp == NULL)
	{
		return 1;
	}

	Head = read_ap_list(rfp);

	sort_for_link_node(&Head);

	write_ap_info(Head);

	fclose(rfp);

	delete_link(&Head);
	return 1;

}


void echo_html_begin(void)
{
    printf("<!DOCTYPE html>\n");
    printf("<html lang=\"en\">\n");
    return;
}

void echo_html_end(void)
{
    printf("</html>\n");
    return;
}

void echo_head()
{
    printf("<head>\n");
    printf("<meta charset=\"UTF-8\">\n");
    printf("<title>设备设置</title>\n");
    printf("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n");
    printf("<link rel=\"stylesheet\" href=\"/css/bootstrap.min.css\">\n");
    printf("<link rel=\"stylesheet\" href=\"/css/style.css\">\n");
    printf("</head>\n");

    return;
}

void echo_use_js(void)
{
    printf("<script type=\"text/javascript\" src=\"/js/jquery.min.js\"></script>\n");
    printf("<script type=\"text/javascript\" src=\"/js/bootstrap.min.js\"></script>\n");
    printf("<script type=\"text/javascript\" src=\"/js/main.js\"></script>\n");

    return;
}

void echo_panel_head(void)
{
    printf("<div class=\"panel-heading\">\n");
    printf("<h1 id=\"title\" class=\"text-center\">设置</h1>\n");
    printf("</div>\n");

    return;
}

void echo_panel_body_form_ssid(void)
{
    char ssid_value[128];
    int has_conn = 0;
    has_conn = get_ap_ssid(ssid_value, 127);
    if (!has_conn)
    {
        has_conn = get_conn_ssid(ssid_value, 127);
        if (!has_conn)
        {
            memcpy(ssid_value, "Domigo-XXXXXONE", strlen("Domigo-XXXXXONE"));
            ssid_value[strlen("Domigo-XXXXXONE")] = '\0';
        }
	}

    printf("<div class=\"form-group\">\n");
    printf("<label class=\"control-label\" for=\"ssid\">SSID:</label>\n");
    printf("<p class=\"form-control-static\">%s</p>\n", ssid_value);
    printf("</div>\n");

    return;
}

void echo_panel_body_form_devname(void)
{
    printf("<div id=\"device-name-group\" class=\"form-group\">\n");
    printf("<label class=\"control-label\" for=\"name\">设备名称：</label>\n");
    printf("<div class=\"input-group\">\n");
    printf("<input id=\"device-name\" type=\"text\" class=\"form-control\" name=\"name\" placeholder=\"请选择或者输入名称\">\n");
    printf("<div class=\"input-group-btn\">\n");
    printf("<button type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\"><span class=\"caret\"></span></button>\n");
    printf("<ul class=\"dropdown-menu centerDropdown\">\n");
    printf("<li><a class=\"name-item\" href=\"#\">客厅</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">卧室</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">书房</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">餐厅</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">办公室</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">客房</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">浴室</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">走廊</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">家庭活动</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">车库</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">花园</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">健身房</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">门厅</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">厨房</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">便携式</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">娱乐室</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">工作室</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">游泳池</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">露台</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">影音房</a></li>\n");
    printf("<li><a class=\"name-item\" href=\"#\">儿童房</a></li>\n");
	printf("<li><a class=\"name-item\" href=\"#\">其他</a></li>\n");
    printf("</ul>\n");
    printf("</div>\n");
    printf("</div>\n");
    printf("</div>\n");

    return;

}


void echo_panel_body_form_network(void)
{
    printf("<div id=\"network-group\" class=\"form-group\">\n");
    printf("<label for=\"network\" class=\"control-label\">需要连接的网络：</label>\n");
    printf("<select id=\"network\" name=\"network\" class=\"form-control\">\n");
    printf("<option value=\"\" selected disabled>请选择网络</option>\n");
    //printf("<option value=\"song-1\" data-password>song-1</option>\n");
    //printf("<option value=\"song-2\">song-2</option>\n");
    get_wifi_list();
    printf("</select>\n");
    printf("</div>\n");

    return;
}

void echo_panel_body_form_netpasswd(void)
{
    printf("<div id=\"password-group\" class=\"form-group hidden\">\n");
    printf("<label for=\"password\" class=\"control-label\">网络密码：</label>\n");
    printf("<input id=\"password\" type=\"password\" name=\"password\" class=\"form-control\" placeholder=\"请输入密码\">\n");
    printf("</div>\n");

    return;

}

void echo_panel_body_form(void)
{
    printf("<form action=\"/cgi-bin/connect\" method=\"post\" role=\"form\">\n");
    echo_panel_body_form_ssid();

    echo_panel_body_form_devname();


    echo_panel_body_form_network();

    echo_panel_body_form_netpasswd();

    printf("<input id=\"connect\" type=\"submit\" value=\"连接\" class=\"btn btn-primary btn-lg btn-block\">");

    printf("</form>");
}

void echo_panel_body(void)
{
    printf("<div class=\"panel-body\">\n");

    echo_panel_body_form();


    printf("</div>\n");

    return;
}

int main(void)
{
    printf("Content-type:text/html\n\n");

    echo_html_begin();

    echo_head();
    printf("<body><div id=\"main\" class=\"container\"><div class=\"panel panel-default\">\n");

    echo_panel_head();

    echo_panel_body();


    printf("</div></div>\n");

    echo_use_js();

    printf("</body>\n");
    echo_html_end();


	return 0;
}

