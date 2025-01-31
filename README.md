GitHub repository link: https://github.com/MrDru912/distributed-system-deadlock-detection/tree/main

# Description
Distributed system enabling basic management of nodes and exchange of resources with deadlock prevention using Local Lomet Algorithm.

Chosen topology is ring. For communication grpc was used, all messages are defined in cmds.proto.

Every node has attributes, main are: ip, port, myNeighbours for remembering naighbours(prev, next, and next after next), grpcServer and myCommHub for sending messages, myCommHub for accepting messages, myAPIHandler for handling rest api, delay, nodeStatus to check if node is blocked by
algorithm, grantedResources is hashmap of resources to node's disposal.
Topology is built by those functions:
* join
* leave
* kill
* revive

Join puts a node into circle, keave is normal removal of a node from circle, kill is abnormal removal of node which can be detected by sending hello to the killed node from previous node using send_hello_next
endpoint which will lead to automatic repairing of topology by changing pointers on neighbours objects. Revive can be called immidietly after kill to restore topology, after some changes it will be not possible
to revive node because network will not have enough information to do so.

Local lomet can be shown on resource accessing. For simplicity every node holds single resource with id ip:port_R. Resources should at first send preliminary request to resource holder to let it know about intention
to get the resource. Then it sends real requests which will result in either granting the resource or deleying the request. According to local lomet each node holds dependency graph which is checked for
cycles after each actual request and thanks to ordered preliminary requests it prevents deadlock from happening. Request also contains acquire, as it results in granted access to resource the resource will be sent
to the requester.

Approximate flow and messages can be shown on example with 2 nodes. Lets say we have 2 nodes/processes p1,p2 which have resources r1,r2 respectively.

* p1 and p2 need r1 to do something with them.

* They first send preliminary requests to r1.

* Messages circulate in the ring until message gets to the holder of desired resource. r1 updates local graph.

* Then lets say r2 happens to send actual request first and sends request for the resource to r1 located on p1 and becomes blocked(passive).

* r1 will update graph and see if a deadlock can happen after granting resource to r2 and finds a cycle dependency in it's graph.

* It reverts changes on the graph and delays r2 request.

* Then p1 sends actual request for r1 and again r1 checks if after assiging resource to p1 the cycle can happen.

* And it finds out that there is no cycle so it grants resource to p1.

* Then p1 after working with r1 releases it which is reflected in local graph by deleting p1.

* r1 processes the delayed request of p2 again and grants access to r1.

* Then after r2 finishes working with r1 releases it and local graph is updated having no more dependencies.

# Build
Maven was used to build, all dependencies are in pom.xml.
* mvn clean compile

# Use
I was building nodes in Intellij IDEA.

Build project using maven compile.

![image](https://github.com/user-attachments/assets/b3c9cf62-859d-4ac8-af11-54d103c1c42f)

Set up configurations for node like on the image and run them. then use rest api to create topology and access resources.

![image](https://github.com/user-attachments/assets/31200cad-1852-4318-afca-df46abbe6d9f)

# Functions(REST API):
* get_status: get
* join: get /join/{other_node_ip}/{other_node_port}
* leave: get /leave
* kill: get /kill
* revive: get /revive
* send_hello_next(for repairing topology): get /send_hello_next
* setDelay: get /setDelay/{delay}
* preliminary_request: get /preliminary_request/{resource_id}
* request_resource: get /request_resource/{resource_id}
* release_resource: get /release_resource/{resource_id}
