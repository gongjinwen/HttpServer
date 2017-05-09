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

    int mode =  (int)get_wpa_wifi_flag_vaule(40);

    printf("{\n");
    printf("\"responseCode\" : \"200\",\n");
    printf("\"dspMode\" : \"%d\",\n", mode);
    printf("\"message\" : \"ok\"\n");
    printf("}\n");

    return 0;
}



