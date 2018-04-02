package entities.physical;

import entities.logical.*;

import java.util.*;

public class Server extends EndDevice{
    private Map<Server,Link> links = new HashMap<>();
//    private Link clientLink;
    private List<IFile> files;
    private int cacheSize = DefaultValues.CACHE_SIZE;
    private Map<EndDevice, Link> routingTable = new HashMap<>();
    private Map<EndDevice, Integer> communicationCostTable = new HashMap<>();
    private Queue<Request> queue = new ArrayDeque<>();





    @Override
    protected boolean isReceivedDataValid(Link link) {
        /***
         * Checks the validity of the link from the segment arrived.
         */
        boolean linkExistence =  links.values().contains(link) ;
        return linkExistence;
    }
    @Override
    protected void parseReceivedSegment(float time, Segment segment) {
        /***
         * takes suitable course of action according to the type of the segment
         */
        if (isThisDeviceDestined(segment)) {
            switch (segment.getSegmentType()) {

                case Request:
                    Request request = (Request) segment.getOptionalContent();
                    queue.add(request);
                    if (queue.size()==1) {
                        setTimerToPopQueue(time);
                    }
                    break;
                case Data:
                default:
                    throw new RuntimeException("Segment dropped. Unexpected file received by server" + toString());
            }
        }else{
            forwardSegment(time, segment );
        }

    }

    private void setTimerToPopQueue(float time) {
        /***
         * releases an event which at current time + service time pops the queue
         */
            if (queue.size()==0) return;
            EventsQueue.addEvent(
                    new Event<>(EventType.pop, this, time + DefaultValues.SERVICE_TIME, this)
            );

    }

    private void forwardRequest(float time, Request request, Server selectedServer) {
        /***
         * Forwards the request to the intended server  - makes new request and segment
         */

        Client client = request.getSource();
        Request newRequest = new Request(client,selectedServer,request.getNeededFileID(),request.getId());
        Segment newSegment = new Segment(newRequest.getId(),this,selectedServer,DefaultValues.REQUEST_SIZE,SegmentType.Request,newRequest);
        forwardSegment(time, newSegment);
    }

    private void forwardSegment(float time, Segment segment) {
        /***
         * Forwards the segment to the intended server using routing table and the corresponding link
         */
        EndDevice destination = segment.getDestination();
        Link link = routingTable.get(destination);
        sendData(time, link , segment);      //Without any delay forward the packet
    }


    private void serveRequest(float time, Request request) {
        /***
         * Checks if the file is cached. If Yes sends the file, otherwise finds another server.
         */
        float queryDelay = 0f; //TODO : update this
        IFile neededFile= findFile(request.getNeededFileID());
        Server selectedServer = getSuitableServer(request);
        if (selectedServer==null) throw new RuntimeException();
        if (selectedServer.equals(this)){
            EndDevice destination = request.getSource();
            Link link = routingTable.get(destination);
            Segment fileSegment = new Segment(request.getId(), this, destination , neededFile.getSize() , SegmentType.Data, neededFile);
            sendData(time+ queryDelay, link, fileSegment);

        }

        forwardRequest(time + queryDelay , request,selectedServer);

    }

    public int getServerLoad(){
        return queue.size();
    }

    private Server getSuitableServer( Request request) {
        /***
         *   finds a suitable server from graph to respond to the request
         */
        int fileId = request.getNeededFileID();
        Client client = request.getSource();
        List<Server> serversHavingFile = SimulationParameters.serversHavingFile.get(fileId);
        if (serversHavingFile==null || serversHavingFile.size()==0) throw new RuntimeException(" ");
        Server selectedServer = RedirectingAlgorithm.selectServerToRedirect(SimulationParameters.redirectingAlgorithmType,serversHavingFile,client);
        return selectedServer;
    }



    public IFile findFile(int fileID){
        /***
        * searches for a file in cached files with corresponding fileID */
        for (IFile f:files) {
            if (f.getId()==fileID){
                return f;
            }
        }
        return null;
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        switch (event.getType()){
            case receiveSegment:
                receiveData(event.getTime(),(Segment) event.getOptionalData() , (Link)event.getCreator());
                break;
            case pop:
                pop(event.getTime());
        }
    }

    private void pop(float time) {
        /***
         * Commands to serve the request then after a service delay serve the next request
         */
        if (queue.size()==0) return;
        Request request = queue.remove(); //popping action
        serveRequest(time, request);
        setTimerToPopQueue(time);
    }

    public Map<EndDevice, Link> getRoutingTable() {
        return routingTable;
    }

    public Map<EndDevice, Integer> getCommunicationCostTable() {
        return communicationCostTable;
    }


    public List<IFile> getFiles() {
        return files;
    }

    public void setFiles(List<IFile> files) {
        this.files = files;
    }

    public Map<Server, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<Server, Link> links) {
        this.links = links;
    }

}
