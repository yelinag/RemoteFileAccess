# RemoteFileAccess

### Introduction

In this project, it is required to design and implement a file access system using client-server architecture. 
Clients are required to be able to access the server physical memory which includes files and directories that 
are stored on server disk.
Clients can access the files and directories through a set of services provided by the server using UDP connection:

*	Read a file on the server (Idempotent)
*	Write to a file one the server (Non-Idempotent)
*	Monitor server files content changes at client side (Idempotent)
*	List Directory (Idempotent)
*	Append file (Non-Idempotent)
