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

int main(void)
{
    attach_share_mem();

    char *data = NULL;
    data = getenv("QUERY_STRING");
    if (NULL == data)
    {
        return -1;
    }

    char *decode_buf = NULL;
    decode_buf = url_decode(data);

    fprintf(stderr, "===decode_buf:%s\n", decode_buf);
    char *mode = NULL;
    if (NULL != decode_buf && strstr(decode_buf, "mode="))
    {
        mode = get_select_str(decode_buf, '=', '\0');
    }

    fprintf(stderr, "=== mode:%s\n", mode);
    int mode_int = atoi(mode);
    set_wpa_wifi_flag(mode_int, 40);
	set_wpa_wifi_flag('5', 58);

    printf("{\n");
    printf("\"responseCode\" : \"200\",\n");
    printf("\"message\" : \"ok\"\n");
    printf("}\n");

    return 0;

}


