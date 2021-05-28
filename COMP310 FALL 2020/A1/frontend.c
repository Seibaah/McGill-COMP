#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "a1_lib.h"

#define BUFSIZE   1024

bool awake=true;

int main(int argc, char *argv[]) {
  int sockfd;
  char str[BUFSIZE] = { 0 };
  char server_msg[BUFSIZE] = { 0 };
  
  //convert and save the port argument
  uint16_t port = (uintptr_t) strtol(argv[2], NULL, 0);

  //connect to the server
  if (connect_to_server(argv[1], port, &sockfd) < 0) {
    fprintf(stderr, "oh no\n");
    return -1;
  }

  /*if the connection is succesfull and while 
  the client hasn't quit we can keep exchanging messages with the server*/
  while (awake) {
     
    int argCount = -1;  //keeps track of the # or arguments sent with a command
    char *tok;    
    char command[128] = {""}, arg1[128]= {""}, arg2[128]= {""}; //default values for the struct

    //allocatingmemory for the server message
    memset(str, 0, sizeof(str));
    memset(server_msg, 0, sizeof(server_msg));
    printf (">> "); 

    //getting user command line and tokenizing it 
    char *line = fgets(str,128,stdin);
    tok = strtok(str, " ");

    //first argument is the command to execute
    if (tok != NULL){
      strcpy(command, tok);
    }

    int i=0;  //counter var to keep track of arguments

    //tokenize the arguments
    while (tok != NULL) {
      
      tok = strtok (NULL, " ");

      //save the first 2 arguments as strings if applicable
      if (i==0 && tok != NULL){
        strcpy(arg1, tok);  
      }  
      if (i==1 && tok != NULL){
        strcpy(arg2, tok);
      }   

      ++argCount; ++i;  //argCount will pass how many arguments were typed alonside a command
    }
  
    struct message msg = {{""}, argCount, {""}, {""}};   //create an "empty" default struct
    
    //fill the struct with the command, at most 2 args and the counter of arguments input
    strncpy(msg.s, command, 128);
    strncpy(msg.arg1, arg1, 128);
    strncpy(msg.arg2, arg2, 128);

    // send the input to server
    send_message(sockfd, (char*)&msg, sizeof(msg));

    // receive a msg from the server
    ssize_t byte_count = recv_message(sockfd, server_msg, sizeof(server_msg));
    if (byte_count <= 0) {
      break;
    }

    /*If the message received is exit or quit then we close the connection
    The loop will not run again due to awake being set to false*/
    if (strcmp(server_msg, "leaving server\0")==0){
      close(sockfd);
      awake=false;
    } else if (strcmp(server_msg, "shutting down server\0")==0){
      close(sockfd);
      awake=false;
    }
    //Print the server message
    printf("Server: %s\n", server_msg);

    //clean stdin and stdout
    fflush(stdin); 
    fflush(stdout);
  }

  return 0;
}

