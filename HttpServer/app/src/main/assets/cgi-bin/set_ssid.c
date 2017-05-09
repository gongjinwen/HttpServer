#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "checkservice.h"

#define MAX_ARG         150

void set_ap_passwd(char *passwd)
{
    if (NULL == passwd)
    {
        return;
    }

    FILE *wfp = NULL;
    wfp = fopen("/tmp/ap.cfg", "wb");
    if (NULL == wfp)
    {
        return;
    }

    fprintf(wfp, "[apconfig]\n");
    fprintf(wfp, "ssid=%s\n", "yy00000");
    fprintf(wfp, "channel=%d\n", 6);
    fprintf(wfp, "encryption=%s\n", "psk");
    if(strlen(passwd) > 1)
    {
        fprintf(wfp, "psk=%s\n", passwd);
    }
    fclose(wfp);

    return;
}

int connect_wifi(char *network, char *passwd, int type)
{
    attach_share_mem();

    FILE *write_file = fopen("/tmp/tmp_wifi.cfg", "wb");
    fprintf(write_file, "ctrl_interface=/var/run/wpa_supplicant\n");
    fprintf(write_file, "update_config=1\n");

    fprintf(write_file, "network={\n");
    fprintf(write_file, "\tssid=\"%s\"\n", network);

    int wep_key_type = 0;
    if (1 == type)   //wep
    {
        if (5 == strlen(passwd) || 13 == strlen(passwd))
        {
            wep_key_type = 1;   //ascii
        }
        else if (10 == strlen(passwd) || 26 == strlen(passwd))
        {
            wep_key_type = 2;  //hex
        }
        else
        {
            type = 2;
        }
    }

    switch(type)
    {
        case 0:
            fprintf(write_file, "\tkey_mgmt=NONE\n");
            fprintf(write_file, "\tpriority=%d\n", 5);
            break;
        case 1:
            fprintf(write_file, "\tkey_mgmt=NONE\n");
            fprintf(write_file, "\twep_tx_keyidx=%d\n", 0);
            if (1 == wep_key_type)
            {
                fprintf(write_file, "\twep_key0=\"%s\"\n", passwd);
            }
            if (2 == wep_key_type)
            {
                fprintf(write_file, "\twep_key0=%s\n", passwd);
            }
            fprintf(write_file, "\tpriority=%d\n", 5);
            break;
        case 2:
            fprintf(write_file, "\tpsk=\"%s\"\n", passwd);
            fprintf(write_file, "\tpriority=%d\n", 5);
            break;
        case 3:
            fprintf(write_file, "\tpsk=\"%s\"\n", passwd);
            fprintf(write_file, "\tpriority=%d\n", 5);
            break;
        default:
            fprintf(write_file, "\tpsk=\"%s\"\n", passwd);
            fprintf(write_file, "\tpriority=%d\n", 5);
            break;
    }
    fprintf(write_file, "}\n");


    if (NULL != write_file)
    {
        fclose(write_file);
    }

    system("cp -af /tmp/tmp_wifi.cfg /tmp/wifi.cfg");
    system("rm -f /tmp/tmp_wifi.cfg");
    system("sync");

    int connect_status = 1;  //0 idle 1:connecting 2:failed 3:success
    set_wpa_wifi_custom_value(192, 192 + sizeof(int), (char *)&connect_status);

    set_wpa_wifi_flag('5', 0);

    return 1;
}


int set_device_name(char *device_name)
{
    if (NULL == device_name)
    {
        return -1;
    }

    char buf[1024];
    FILE *read_file = NULL;
    FILE *rfp = NULL;
    int iswirte = 0;

    if (0 == access("/tmp/tmp_write_system_cfg", F_OK))
    {
        return 0;
    }

    system("cp -f /tmp/system.cfg /tmp/tmp_read_system_cfg");

    read_file = fopen("/tmp/tmp_read_system_cfg","rb");
    rfp = fopen("/tmp/tmp_write_system_cfg","wb");

    if(read_file == NULL)
    {
        fprintf(rfp, "[system]\n");
        fprintf(rfp, "name=%s\n", device_name);
    }

    while(!feof(read_file))
    {
        memset(buf, 0, sizeof(buf));
        fgets(buf, 1024, read_file);

        if(!*buf)
        {
            continue;
        }
        buf[strlen(buf)]='\0';

        if(strstr(buf, "name="))
        {
            fprintf(rfp, "name=%s\n", device_name);
            iswirte = 1;
            continue;
        }

        fprintf(rfp, "%s", buf);
    }

    if(!iswirte)
    {
        fprintf(rfp, "[system]\n");
        fprintf(rfp, "name=%s\n", device_name);
    }



    if(NULL != read_file)
    {
        fclose(read_file);

    }

    if(NULL != rfp)
    {
        fclose(rfp);
    }

    system("rm -f /tmp/system.cfg");
    system("mv /tmp/tmp_write_system_cfg /tmp/system.cfg");
    system("rm -f /tmp/tmp_read_system_cfg");

    return 0;
}


int main(void)
{
    char *data = NULL;
    data = getenv("QUERY_STRING");
    if (NULL == data)
    {
        return -1;
    }

    char *decode_buf = NULL;
    decode_buf = url_decode(data);


    char *name = NULL;
    char *network = NULL;
    char *passwd = NULL;
    char *type_str = NULL;
    char *tmp = NULL;
    if (NULL != decode_buf && strstr(decode_buf, "name="))
    {
        name = get_select_str(decode_buf, '=', '&');
        tmp = strstr(name, "&network=");
        if (NULL != tmp)
        {
            name[tmp - name] = '\0';
        }
    }
    if (NULL != decode_buf && strstr(decode_buf, "&network="))
    {
        network = get_select_str(strstr(decode_buf, "&network="), '=', '&');
        tmp = strstr(network, "&ap_type=");
        if (NULL != tmp)
        {
            network[tmp - network] = '\0';
        }
    }

    if (NULL != decode_buf && strstr(decode_buf, "&ap_type="))
    {
        type_str = get_select_str(strstr(decode_buf, "&ap_type="), '=', '&');
    }

    if (NULL != decode_buf&& strstr(decode_buf, "&password="))
    {
        passwd = get_select_str(strstr(decode_buf, "&password="), '=', '\0');
    }

    //printf("<h2>name:%s</h2>\n", name);
    //printf("<h2>network:%s</h2>\n", network);
    //printf("<h2>passwd:%s</h2>\n", passwd);
    //printf("<h2>type_str:%s</h2>\n", type_str);

    //set dlna device name
    set_device_name(name);

    if (strlen(network) > 1)
    {
        connect_wifi(network, passwd, atoi(type_str));
        set_ap_passwd(passwd);
    }

    printf("{\n");
    printf("\"respCode\" : \"0\"\n");
    printf("}\n");

    return 0;

}




