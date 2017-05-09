#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "checkservice.h"
#include "cJSON.h"

int main(void)
{
    FILE* fp = NULL;
    if((fp = popen("df  | grep sdcard | awk '{print $4}'", "r")) == NULL)
    {
        return -1;
    }

    char buf[512];
    int len = 0;
    memset(buf, 0, sizeof(buf));
    if (fgets(buf, 512, fp) != NULL && strlen(buf) > 1)
    {
        len = strlen(buf) - 1;
	    buf[len] = '0';
        pclose(fp);

        printf("{\n");
        printf("\"message\" : \"ok\",\n");
        printf("\"responseCode\" : \"200\",\n");
        char tmp[256];
        memset(tmp, 0, sizeof(tmp));
        sprintf(tmp, "\"freeSpace\" : \"%s\"\n", buf);
        printf("%s", tmp);
        printf("}\n");    
        return 0;
    }
	
    printf("{\n");
    printf("\"message\" : \"ok\",\n");
    printf("\"responseCode\" : \"200\",\n");
    printf("\"freeSpace\" : \"0\"\n");
    printf("}\n");    

    return 0;
}


