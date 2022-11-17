### About the assignement:

**IMPORTANT Considerations**

Unit tests were added for the AggregationRestController. This is not the case for all classes to make the assignment less time consuming. By Unit Test must be understood testing the execution of code units, most likely methods. This includes injecting mocks as much as possible and verifying the interaction of the test subject with them. Check of the return values are to be taken into account of course. 

The few unit tests present are to be considered as a base for discussion. Be sure that the distinction with integration tests is understood, the latter involving testing of the interaction with the components such as Controller, Services and the code execution as close as possible from reality.


### Design insights:

- Framework choice is Spring Boot for the simplicity it offers to build a Rest Controller.
- One Aggregation Controller with one Method to handle requests using one dedicated service per remote end point (pricing, track and shipments).
- The Remote APIs serving data retrieved as Map for all of them, choice was made to build an abstract service that would share everything the querying has in common.
- Any request for an aggegration will see any missing GET parameter responded with an empty Map.
- The data Being fetched as Map, The abstract service is typed with the key and value Generic variables to make the concrete subclasses as simple to declare as can be. Any instance of a client service of another end point serving data that could fit into a Map would be easier to declare (so far as long as the remote service uses q as GET parameter so far).
- The concurrency is managed using an Executor bean to allow asynchronous methods in the Services and to offer more control over Thread execution.
- The RestController request CompletableFutures from its services and waits for their completion prior to return an response
- Any request leads to add a pending CompletableFuture to the involved services, if the request is empty due to a GET parameter omission to the Rest Controller, the CompletableFuture is immediately completed with and empty Map. This mean that missing GET parameter (pricing, track and shipments) will not lead to a pending CompletableFuture to complete.
- Each client Service will submit a FetchRunnable to the Executor Bean once the queue contains more than 5 items to query or when pending request are present since too long (5seconds)
- To perform the timeout check and potential fetching, a Spring Boot TaskScheduler and enabled. The actual timeout check and potential subsequent fetching of remote data is done in the @Scheduled timer method of the AbstractController
