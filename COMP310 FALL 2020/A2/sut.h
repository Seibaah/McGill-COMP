#ifndef __SUT_H__
#define __SUT_H__
#include <stdbool.h>
#include <stdlib.h>
#include <ucontext.h>
#include <pthread.h>
#include <unistd.h>

#define MAX_MSG_SIZE 1024

typedef void (*sut_task_f)();

//c_exec task struct
typedef struct __threaddesc
{
	int threadid;
	int sockfd;
	void *threadfunc;
	bool mem_allocated;
	bool connection_open;
	ucontext_t threadcontext;
} threaddesc;

//io task struct
typedef struct __iothread
{
	int c_exec_id;
	int size;
	char *instruction;
	char message[1024];
	char *addr;
	int port;
} iothread;

/******************************
Create the 2 kernel threads
*******************************/
void sut_init();

/******************************
Package function into a task and add it to the ready queue
*******************************/
bool sut_create(sut_task_f fn);

/******************************
Pauses a thread execution and puts it back into the ready queue. 
User task context is saved in a Task control block (TCB) and then
the next task is loaded and runs
*******************************/
void sut_yield();

/******************************
Stop a thread execution and don't put it back into the ready queue. 
User task context is not saved in a Task control block (TCB).
The next task is loaded and runs
*******************************/
void sut_exit();

/******************************
Establishes connection with the remote shell
*******************************/
void sut_open(char *dest, int port);

/******************************
Open and use the write stream to the remote shell
*******************************/
void sut_write(char *buf, int size);

/******************************
Closes connection with the remote shell
*******************************/
void sut_close();

/******************************
Open and use the read stream to the remote shell
*******************************/
char *sut_read();

/*
Pauses main thread until c_exec and i_exec are done
*/
void sut_shutdown();

/*
c_exec task scheduler
*/
void *cexecQueue();

/*
i_exec io operation scheduler
*/
void *iexecQueue();


#endif
