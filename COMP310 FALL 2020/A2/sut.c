#include <stdio.h>
#include <stdlib.h>
#include <ucontext.h>
#include <pthread.h>
#include "sut.h"
#include "a1_lib.h"
#include "queue.h"

//pthread and queue variables
pthread_t id[2];
struct queue ready_q, io_q;

//tasks arrays for c_exec and io
threaddesc c_exec_tasks[20];
iothread i_exec_tasks[100];

//context variables
ucontext_t c_exec_ctx, i_exec_ctx;

//c_exec variables
int c_task_id=0, c_creation_index=0, c_curr_index, c_tasks_completed;

//i_exec variables
int i_task_id=0, i_creation_index=-1, sockfd;
char message[MAX_MSG_SIZE], temp[MAX_MSG_SIZE];

//main thread shutdown flag
bool shutdown_flag=false, c_exec_done=false, i_exec_done=false;

//queue locks
pthread_mutex_t queue_lock =  PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t i_queue_lock = PTHREAD_MUTEX_INITIALIZER;

//initailze queues and start pthreads
void sut_init(){
    
	//c_exec queue creation
	ready_q = queue_create();
    queue_init(&ready_q);

	//io queue creation
	io_q = queue_create();
    queue_init(&io_q);
	
	//iexec thread creation
	getcontext(&i_exec_ctx);
    pthread_create(&id[1], NULL, iexecQueue, NULL);

	//cexec thread creation
	getcontext(&c_exec_ctx);
	pthread_create(&id[0], NULL, cexecQueue, NULL);
}

//polling function for c_exec
void *cexecQueue(){
	while (1){
		pthread_mutex_lock(&queue_lock);
		struct queue_entry *p = queue_peek_front(&ready_q);
		if (p==NULL) {
			pthread_mutex_unlock(&queue_lock);

			//c_exec termination condition
			if (c_tasks_completed==c_task_id && shutdown_flag==true){
				c_exec_done=true;
				pthread_exit(0);
			}
			usleep(100);
		}
		else {
			//pop pending tasks in the queue
			struct queue_entry *ptr = queue_pop_head(&ready_q);
			pthread_mutex_unlock(&queue_lock);

			//unpack the queue node
			threaddesc *task = (threaddesc*) ptr->data;

			//unpack the struct
			ucontext_t task_context = (ucontext_t) task->threadcontext;
			c_curr_index=task->threadid;

			//run the task
			swapcontext(&c_exec_ctx, &task_context);	
		}
	}
}

//create tasks for c_exec
bool sut_create(sut_task_f fn){
	//assign tassk fn and identifier
	c_exec_tasks[c_creation_index].threadfunc = fn;
	c_exec_tasks[c_creation_index].threadid=c_task_id;
	c_task_id++;

	//alocate memory for context and initialize it 
	getcontext(&c_exec_tasks[c_creation_index].threadcontext);
	c_exec_tasks[c_creation_index].threadcontext.uc_stack.ss_sp = calloc(16, 1024);
	c_exec_tasks[c_creation_index].threadcontext.uc_stack.ss_size = 16 * 1024;
	c_exec_tasks[c_creation_index].threadcontext.uc_link = &c_exec_ctx;
	makecontext(&c_exec_tasks[c_creation_index].threadcontext, fn, 0);

	c_exec_tasks[c_creation_index].mem_allocated=true;

	//add task struct to ready_q
    struct queue_entry *node = queue_new_node(&c_exec_tasks[c_creation_index]);
	c_creation_index++;

	pthread_mutex_lock(&queue_lock);
    queue_insert_tail(&ready_q, node);
	pthread_mutex_unlock(&queue_lock);

	return true;
}

//yields thread ressource to c_exec
void sut_yield(){
	//saves the task progress
	//makecontext(&c_exec_tasks[c_curr_index].threadcontext, c_exec_tasks[c_curr_index].threadfunc, 0);

	//add it back in the ready_q
	struct queue_entry *node2 = queue_new_node(&c_exec_tasks[c_curr_index]);

	pthread_mutex_lock(&queue_lock);
    queue_insert_tail(&ready_q, node2);
	pthread_mutex_unlock(&queue_lock);

	//swap back to the task scheduler c_exec
	swapcontext(&c_exec_tasks[c_curr_index].threadcontext, &c_exec_ctx);
}

//terminates task and liberates ressources
void sut_exit(){
	//deallocate the memory, reset task flag
	free(c_exec_tasks[c_curr_index].threadcontext.uc_stack.ss_sp);
	c_exec_tasks[c_creation_index].mem_allocated=false;

	//update global task progression and resume the task scheduler c_exec
	c_tasks_completed++;
	setcontext(&c_exec_ctx);
}

//pauses the main thread until c_exec and i_exec are done
void sut_shutdown(){
	shutdown_flag=true;
	pthread_join(id[0], NULL);
	pthread_join(id[1], NULL);

	//backup free in case memory deallocation failed in sut_exit
	for (int i=0; i<c_tasks_completed; i++){
		if (c_exec_tasks[i].mem_allocated==false){
			free(c_exec_tasks[i].threadcontext.uc_stack.ss_sp);
		}
	}
}

//io operations scheduler
void *iexecQueue(){
	while(1){

		pthread_mutex_lock(&i_queue_lock);
		struct queue_entry *p = queue_peek_front(&io_q);

		if (p==NULL){
			pthread_mutex_unlock(&i_queue_lock);

			//i_exec termination condition
			if (c_exec_done==true){
				pthread_exit(0);
			}

			usleep(100);

		}
		else {
			//pop the io node
			struct queue_entry *ptr = queue_pop_head(&io_q);
			pthread_mutex_unlock(&i_queue_lock);

			//unpack the node's data
			iothread *task = (iothread*) ptr->data;
			
			//the io nodes have a string field to indicate which actions to execute
			if(strcmp(task->instruction, "open\n")==0){
				uint16_t pt = (uintptr_t) task->port;

				//connect to the server
				if (connect_to_server(task->addr, pt,  &sockfd) < 0) {
					fprintf(stderr, "Connection error\n");
				} else c_exec_tasks[task->c_exec_id].connection_open = true;

				//regardless of sucess put the task back in the ready_q. 
				//The suces flag will determine dependent behavior
				struct queue_entry *node = queue_new_node(&c_exec_tasks[i_exec_tasks[i_creation_index].c_exec_id]);

				pthread_mutex_lock(&queue_lock);
				queue_insert_tail(&ready_q, node);
				pthread_mutex_unlock(&queue_lock);

			} 
			else if(strcmp(task->instruction, "read\n")==0){
				int min_bytes_to_read=3;
				
				//reset the message buffers
				memset(message,0,sizeof(message));
				memset(temp,0,sizeof(temp));

				/*
				the remote shell returns $ and a termination character
				thus to read what comes after one must read twice.
				If the message hasn't changed the second time we break to 
				not get stuck waiting 
				*/
				while (1){
					
					if (min_bytes_to_read>0){

						int n = recv_message(sockfd, temp, sizeof(task->message));
						
						//if message packet is identical to last we break
						if (strcmp(temp, message)==0){
							break;
						}

						//if no bytes received break
						if (n==0){
							break;
						}
						
						//else we append the new message packet to the global buffer
						strcat(message, temp);

						//reset the packet buffer
						memset(temp,0,sizeof(temp));

						//updates the min bytes left to read
						min_bytes_to_read-=n;
					} 
					else {
						break;
					}
				}

				//add back the calling task to c_exec
				struct queue_entry *node = queue_new_node(&c_exec_tasks[task->c_exec_id]);

				pthread_mutex_lock(&queue_lock);
				queue_insert_tail(&ready_q, node);
				pthread_mutex_unlock(&queue_lock);	

				//printf("\nMessage received: %s", message);	

			} 
			else if(strcmp(task->instruction, "write\n")==0){
				//send a buffer to the server
				if (send_message(sockfd, task->message, task->size) == -1){
					printf("send failure\n");
				} 
				//else printf("\nMessage sent: %s", task->message);

			} 
			else if(strcmp(task->instruction, "close\n")==0){
				//close socket
				close(sockfd);

				//update connection status flag
				c_exec_tasks[task->c_exec_id].connection_open = false;
			}
		}
	}
}

//create open connection request for io
void sut_open(char *addr, int p){
	i_creation_index++;
	if (i_creation_index>=100){
		i_creation_index=0;
	}

	//create an io task
	i_exec_tasks[i_creation_index].addr=addr;
	i_exec_tasks[i_creation_index].port=p;
	i_exec_tasks[i_creation_index].instruction="open\n";
	i_exec_tasks[i_creation_index].c_exec_id=c_curr_index;

	//add the request to the io_q
	struct queue_entry *node = queue_new_node(&i_exec_tasks[i_creation_index]);

	pthread_mutex_lock(&i_queue_lock);
	queue_insert_tail(&io_q, node);
	pthread_mutex_unlock(&i_queue_lock);

	//in the meantime resume c_exec
	swapcontext(&c_exec_tasks[c_curr_index].threadcontext, &c_exec_ctx);
}

//create a write request for io
void sut_write(char *buf, int size){

	//write can only be requested if a connection was succesfully opened
	if(c_exec_tasks[c_curr_index].connection_open==false){
		printf("Error: Open connection required!\n");
	} else {
		i_creation_index++;
		if (i_creation_index>=100){
			i_creation_index=0;
		}
		//create a write request task
		i_exec_tasks[i_creation_index].c_exec_id=c_curr_index;
		i_exec_tasks[i_creation_index].size=size;
		memset(i_exec_tasks[i_creation_index].message,0,
			sizeof(i_exec_tasks[i_creation_index].message));
		strcpy(i_exec_tasks[i_creation_index].message, buf);
		i_exec_tasks[i_creation_index].instruction="write\n";

		//add the request to the io_q
		struct queue_entry *node = queue_new_node(&i_exec_tasks[i_creation_index]);

		pthread_mutex_lock(&i_queue_lock);
		queue_insert_tail(&io_q, node);
		pthread_mutex_unlock(&i_queue_lock);
	}
}

//create a read request for io
char *sut_read(){

	//read can only be requested if a connection was succesfully opened
	if(c_exec_tasks[c_curr_index].connection_open==false){
		printf("Error: Open connection required!\n");
	} else {
		i_creation_index++;
		if (i_creation_index>=100){
			i_creation_index=0;
		}
		//create a read request task
		i_exec_tasks[i_creation_index].c_exec_id=c_curr_index;
		i_exec_tasks[i_creation_index].instruction="read\n";

		//add the request to the io_q
		struct queue_entry *node = queue_new_node(&i_exec_tasks[i_creation_index]);

		pthread_mutex_lock(&i_queue_lock);
		queue_insert_tail(&io_q, node);
		pthread_mutex_unlock(&i_queue_lock);

		//save the context of the task it will be added back to c_exec 
		//after i_exec completes reading in the meantime swap back to c_exec
		swapcontext(&c_exec_tasks[c_curr_index].threadcontext, &c_exec_ctx);
	}
	return message;
}

//creates a close socket request for io
void sut_close(){

	//close can only be requested if a connection was succesfully opened
	if(c_exec_tasks[c_curr_index].connection_open==false){
		printf("Error: Open connection required!\n");
	} else {

		i_creation_index++;
		if (i_creation_index>=100){
			i_creation_index=0;
		}

		//create a close socket request
		i_exec_tasks[i_creation_index].c_exec_id=c_curr_index;
		i_exec_tasks[i_creation_index].instruction="close\n";

		//add the request to the io_q
		struct queue_entry *node = queue_new_node(&i_exec_tasks[i_creation_index]);

		pthread_mutex_lock(&i_queue_lock);
		queue_insert_tail(&io_q, node);
		pthread_mutex_unlock(&i_queue_lock);
	}
}
