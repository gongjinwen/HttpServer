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


int main(void)
{
    attach_share_mem();

    int has_conn = 0;
    char ssid_value[128] = {0};
    has_conn = get_conn_ssid(ssid_value, 127);

    printf("{\n");

    char value[8];
    get_wpa_wifi_custom_value(192, 192 + sizeof(int), value);
    int status = *(int *)(value);
    switch(status)
    {
        case 0:
            printf("\"respCode\" : \"101\",\n");
            printf("\"ssid\" : \"\"\n");
            break;
        case 1:
            printf("\"respCode\" : \"102\",\n");
            printf("\"ssid\" : \"\"\n");
            break;
        case 2:
            printf("\"respCode\" : \"103\",\n");
            printf("\"ssid\" : \"\"\n");
            break;
        case 3:
            printf("\"respCode\" : \"104\",\n");
            printf("\"ssid\" : \"%s\"\n", ssid_value);
            break;
        default:
            printf("\"respCode\" : \"101\",\n");
            printf("\"ssid\" : \"\"\n");
            break;
    }


    printf("}\n");

    return 0;
}

