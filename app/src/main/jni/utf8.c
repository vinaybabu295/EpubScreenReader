//
//  utf8.c
//  training
//
//  Created by Conrad Kleinespel on 5/27/13.
//  Copyright (c) 2013 Conrad Kleinespel. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "utf8.h"

int32_t utf8_validate(char * s) {
    int32_t i = 0;
    size_t len = strlen(s);
    
    while (i < len) {        
        size_t num_bytes = utf8_num_bytes(s + i);
        
        if (num_bytes) {
            i += num_bytes;
        } else {
            return 0;
        }
    }
    return 1;
}

int32_t utf8_is_single_byte(char * c) {
    return (c[0] & 0x80) == 0x0;
}

int32_t utf8_is_double_byte(char * c) {
    return (c[0] & 0xe0) == 0xc0 && utf8_is_continuation(c[1]);
}

int32_t utf8_is_triple_byte(char * c) {
    return (c[0] & 0xf0) == 0xe0 && utf8_is_continuation(c[1]) && utf8_is_continuation(c[2]);
}

int32_t utf8_is_quadruple_byte(char * c) {
    return (c[0] & 0xf8) == 0xf0 && utf8_is_continuation(c[1]) && utf8_is_continuation(c[2]) && utf8_is_continuation(c[3]);
}

int32_t utf8_is_continuation(char c) {
    return (c & 0xc0) == 0x80;
}

size_t utf8_strlen(char * s) {
    size_t i = 0, len = 0;
    while(s[i]) {
        if ( ! utf8_is_continuation(s[i])) ++len;
        ++i;
    }
    return len;
}

char * utf8_remove_trailing_newline(char * s) {
    size_t len = strlen(s);
    char * new_string = NULL;
    
    if (s[len - 1] == '\n') {
        new_string = malloc((len) * sizeof(char));
        memcpy(new_string, s, len);
        new_string[len - 1] = 0x0;
    } else {
        new_string = malloc((len + 1) * sizeof(char));
        strcpy(new_string, s);
    }
    
    return new_string;
}

size_t utf8_num_bytes(char * s) {
    size_t len = strlen(s), num_bytes = 0;
    
    // is valid single byte (ie 0xxx xxxx)
    if (len >= 1 && utf8_is_single_byte(s)) {
        num_bytes = 1;
        
    // or is valid double byte (ie 110x xxxx and continuation byte)
    } else if (len >= 2 && utf8_is_double_byte(s)) {
        num_bytes = 2;
        
    // or is valid tripple byte (ie 1110 xxxx and continuation byte)
    } else if (len >= 3 && utf8_is_triple_byte(s)) {
        num_bytes = 3;
        
    // or is valid tripple byte (ie 1111 0xxx and continuation byte)
    } else if (len >= 4 && utf8_is_quadruple_byte(s)) {
        num_bytes = 4;
    }
    
    return num_bytes;
}

char * utf8_remove_char(char * s, size_t n) {
    size_t len = strlen(s);
    if (len < n) {
        exit(EXIT_FAILURE);
    }
    
    size_t num_shifts = utf8_num_bytes(s + n);
    char * new_string = NULL;
    new_string = malloc(len * sizeof(char));
    
    memcpy(new_string, s, n);
    memcpy(new_string + n, s + n + num_shifts, len - n - num_shifts + 1);
    
    return new_string;
}

char * utf8_add_char(char * s, char * c, size_t n) {
    size_t len = strlen(s);
    if (len < n) {
        exit(EXIT_FAILURE);
    }
    
    size_t num_shifts = utf8_num_bytes(c);
    char * new_string = NULL;
    new_string = malloc((len + num_shifts + 1) * sizeof(char));
    
    // copy the begining of the string
    memcpy(new_string, s, n);
    
    // add the new char
    memcpy(new_string + n, c, num_shifts);
    
    // copy the remaining characters
    memcpy(new_string + n + num_shifts, s + n, len - n + 1);
    
    return new_string;
}

char * utf8_replace(char * needle, char * replace, char * haystack) {
    size_t
        len_replace = strlen(replace),
        len_needle = strlen(needle),
        len = strlen(haystack);
    
    int32_t diff = (int32_t) (len_replace - len_needle);
    
    char * new_string = calloc((len + diff + 1), sizeof(char));
    
    char * pos = strstr(haystack, needle);
    
    if (pos == NULL) {
        strcpy(new_string, haystack);
        return new_string;
    }
    
    size_t num_shifts = pos - haystack;
    
    // Add begining of the string
    memcpy(new_string, haystack, num_shifts);
    
    // Copy the replacement in place of the needle
    memcpy(new_string + num_shifts, replace, len_replace);
    
    // Copy the remainder of the initial string
    memcpy(new_string + num_shifts + len_replace, pos + len_needle, len - num_shifts - len_needle);
    
    return new_string;
}

char * utf8_replace_all(char * needle, char * replace, char * haystack) {
    char
        * new_string = utf8_replace(needle, replace, haystack),
        * old_new_string = NULL;


   // printf("\nFirst:%s\n", new_string);
    if (strstr(new_string, needle) ==NULL){
        return new_string;
    }
    while (strstr(new_string, needle) != NULL) {
        old_new_string = new_string;
        new_string = utf8_replace(needle, replace, new_string);
        free(old_new_string);
        //printf("\n%s", new_string);
    }
    
    return new_string;
}

// the length here is the wanted length of the string, not including the terminating null byte
char * utf8_escape_null_bytes(const char * s, size_t num) {
    char * new_string = NULL;
    // double the amount of available space in case we have only null bytes
    size_t new_size = (num * 2 + 1) * sizeof(char);
    new_string = malloc(new_size);
    memset(new_string, '\0', new_size);
    
    // count number of null bytes
    size_t
        num_null_bytes = 0,
        num_from_s = 0;

    while (num_from_s < num) {
        
        if (s[num_from_s] == 0x0) {
            new_string[num_from_s + num_null_bytes] = '\\';
            new_string[num_from_s + num_null_bytes + 1] = '0';
            num_null_bytes++;
        } else {
            new_string[num_from_s + num_null_bytes] = s[num_from_s];
        }
        
        num_from_s++;
    }
    
    return new_string;
}
