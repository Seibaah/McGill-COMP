#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>


#include "a1_lib.h"

#define BUFSIZE   1024

//declaring the server functions
int addInts(int a, int b);
int multiplyInts(int a, int b);
float divideFloats(float a, float b);
int sleep2(int s);
uint64_t factorial(int x);

int quit=0;

int main(int argc, char *argv[]) {
  int sockfd, clientfd;
  char msg[BUFSIZE];

  int status=1; //status var for child processes return

  //convert port str to uint
  uint16_t port = (uintptr_t) strtol(argv[2], NULL, 0);

  //get parent pid
  pid_t pidA=getpid();

  //create server
  if (create_server(argv[1], port, &sockfd) < 0) {
    fprintf(stderr, "oh no\n");
    return -1;
  } 
  printf("Server created at %s:%hu\n", argv[1], port);  //print server creation data
  
  //main server loop
  while(status!=0){

      if (pidA>0){  //parent process
        if (accept_connection(sockfd, &clientfd) < 0) { //accept incoming connections
          printf("oh no\n");  
          return -1;      
        }  
        quit=waitpid(0, &status, WNOHANG);  //wait for child return. WNOHANG means non blocking
        //if return status is 0 close clientfd
        if(status==0){
          close(clientfd);
          return 0;
        }
      }

      if ((pidA = fork()) == 0){  //child process
        
        //client process main loop
        while (1) {
          fflush(stdin);
          fflush(stdout);

          //alloacte memory for the client message
          char res[1024] = {""};
          memset(msg, 0, sizeof(msg));

          //receive message
          ssize_t byte_count = recv_message(clientfd, msg, BUFSIZE);
          if (byte_count <= 0) {
            break;
          } 

          //save the message in a struct
          struct message *data = (struct message*) msg;

          //if else that compares the commande string to execute its respective func
          if (strcmp(data->s, "add")==0 || strcmp(data->s, "add\n")==0){
            if (data->argCount==2){
              int a = strtol(data->arg1, NULL, 0);  //convert string to int
              int b = strtol(data->arg2, NULL, 0);
              int r = addInts(a,b);                 //call add method
              int x=sprintf(res, "%d", r);          //save the result as a string
            } else {  //if number of arguments doesn't match the function requirements return an error msg
                strcpy(res, "Error: Only 2 arguments accepted with add\0");
            }
          }
          else if (strcmp(data->s, "multiply")==0 || strcmp(data->s, "multiply\n")==0){
             if (data->argCount==2){
              int a = strtol(data->arg1, NULL, 0);  //convert str to int
              int b = strtol(data->arg2, NULL, 0);
              int r = multiplyInts(a,b);            //call mult
              int x=sprintf(res, "%d", r);          //save res in a str
            } else {  
                strcpy(res, "Error: Only 2 arguments accepted with multiply\0");  //# args error
            }
          } 
          else if (strcmp(data->s, "divide")==0 || strcmp(data->s, "divide\n")==0){
            if (data->argCount==2){
              float a = strtof(data->arg1, NULL);   //conv str to float
              float b = strtof(data->arg2, NULL);
              if (b!=0){                            //can't divide by 0
                float r = divideFloats(a,b);        //calc division
                int x=sprintf(res, "%f", r);        //store res in a str
              } else strcpy(res, "Error: Division by zero\0"); //div by 0 err
            } else {  
                strcpy(res, "Error: Only 2 arguments accepted with divide\0");  //# args err
            }
          } 
          else if (strcmp(data->s, "factorial")==0 || strcmp(data->s, "factorial\n")==0){
            if (data->argCount==1){
              uint64_t a = strtoul(data->arg1, NULL, 0);  //conv str to uint
              uint64_t r = factorial(a);                  //call factorial
              int x=sprintf(res, "%lu", r);               //conv res to str
            } else {  
                strcpy(res, "Error: Only 1 argument accepted with factorial\0");  //# args err
            }
          } 
          else if (strcmp(data->s, "sleep")==0 || strcmp(data->s, "sleep\n")==0){
            if (data->argCount==1){
              int a = strtol(data->arg1, NULL, 0);        //convert str to int
              if (a>=0){                                  //can't sleep negative time
                sleep2(a);                                //call sleep
              } else strcpy(res, "Error: Argument must be positive for sleep\0"); //can't sleep negative time
            } else {  
                strcpy(res, "Error: Only 1 argument accepted with sleep\0");  //# args err
            }
          } 
          else if (strcmp(data->s, "exit\n")==0){
            if (data->argCount==0){
              //send goodbye message
              strcpy(res, "leaving server\0");
              send_message(clientfd, res, sizeof(res));
              exit(1);  //return exit status 1
            } else {  
                strcpy(res, "Error: No argument accepted with exit\0");
            }
          } 
          else if ((strcmp(data->s, "quit\n")==0 || strcmp(data->s, "shutdown\n")==0)){
            if (data->argCount==0){
              //send goodbye message
              strcpy(res, "shutting down server\0");  
              send_message(clientfd, res, sizeof(res));
              
              close(clientfd); 
              shutdown(sockfd, SHUT_RDWR);  //shutdown socket
              exit(0);  //return exit status 0 -> alert shutdown
            } else {  
                strcpy(res, "Error: No argument accepted with quit\0");
            }
          } 
          else {
            //if the input is unknown
            sprintf(res, "Error: Command \"%s\" not found", strtok(data->s, "\n"));
            //strcpy(res, "Command not found\0");
          }
          send_message(clientfd, res, sizeof(res)); //send the message back to the client
        }
        
      }
  }
  
  close(sockfd);  //close the socket
  return 0;
}

//All server the functions are listed below. Functionality should be clear as water
int addInts(int a, int b){
  return a+b;
}

int multiplyInts(int a, int b){
  return a*b;
}

float divideFloats(float a, float b){
  return (float) a/b;
}

int sleep2(int s){
  usleep(s*1000000);
  return 0;
}

uint64_t factorial(int x){
  uint64_t res=1;
  while (x>0){
    res*=x; --x;
  }
  return res;
}

