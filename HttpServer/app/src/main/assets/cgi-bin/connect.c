#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "checkservice.h"




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

    read_file = fopen("/tmp/system.cfg","rb");
    rfp = fopen("/tmp/tmp.cfg","wb");

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

    system("rm -f /tmp/system.cfg");
    system("mv  /tmp/tmp.cfg  /tmp/system.cfg");

    if(NULL != read_file)
    {
        fclose(read_file);
    }

    if(NULL != rfp)
    {
        fclose(rfp);
    }

    return 0;
}

void echo_html_begin()
{
    printf("<!DOCTYPE html>\n");
    printf("<html lang=\"en\">\n");
    return;
}

void echo_html_end()
{
    printf("</html>\n");
    return;
}

void echo_head()
{
    printf("<head>\n");
    printf("<meta charset=\"UTF-8\">\n");
    printf("<title>设置成功</title>\n");
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

int main(void)
{
    printf("Content-type:text/html\n\n");

    echo_html_begin();
    echo_head();

    printf("<body>\n");

	char *data;
	int content_len = 0;
	data = getenv("CONTENT_LENGTH");
	if (NULL == data)
	{
	    return -1;
	}
    content_len = atoi(data);

	char content_buf[1024];
	memset(content_buf, 0, sizeof(content_buf));

    char *decode_buf = NULL;
	int read_len = fread(&content_buf, sizeof(char), content_len, stdin);
	if (read_len != content_len)
	{
		return -2;
	}
	else
	{
        decode_buf = url_decode(content_buf);
		//printf("<h2>content_buf:%s</h2>\n", content_buf);
        //printf("<h2>url decode content_buf:%s</h2>\n", decode_buf);
	}

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

    connect_wifi(network, passwd, atoi(type_str));

    printf("<div id=\"main\" class=\"container\">\n");
    printf("<div class=\"panel panel-default\">\n");
    printf("<div class=\"panel-heading\">\n");
    printf("<h1 id=\"title\" class=\"text-center\">设置成功</h1>\n");
    printf("</div>\n");

    printf("<div id=\"connect-notice\" class=\"panel-body\">\n");
    printf("<p>设置成功，设备正在连接网络 <strong id=\"network-name\">%s</strong>...</p>\n",network);

    printf("<ul>\n");
    printf("<li>\n");
    printf("<p>如果设备语音提示“连接已成功”，请将手机的连接到网络 <strong id=\"network-name\">%s</strong></p>\n", network);
    printf("</li>\n");

    printf("<li>\n");
    printf("<p>如果设备语言提示“连接失败”，请 <a href=\"setting\">重试</a></p>");
    printf("</li>\n");
    printf("</ul>");
    printf("</div>");
    printf("</div>");
    printf("</div>");

    echo_use_js();

    printf("</body>\n");
    echo_html_end();

    return 0;

}


