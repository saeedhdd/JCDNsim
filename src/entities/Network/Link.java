/*
 * Developed By Hamid Ghasemi, INL lab, Sharif University of Technology: www.inl-lab.net
 * Copyright (c) 2019. All rights reserved.
 *
 */

package entities.Network;

import entities.Setting.DefaultValues;
import entities.Setting.EventType;
import entities.Simulator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Link extends IEventHandler{
    private EndDevice endPointA;
    private EndDevice endPointB;
    private float propagationDelay;
    private float bw;
    private List<Segment> segmentsFromA;
    private List<Segment> segmentsFromB;
    private boolean isSendingFromA;
    private boolean isSendingFromB;
    private int weight = 1; //number of intervening ASes between endpointA and B+1
    private EventsQueue eventsQueue;

    public int getWeight() {
        return weight;
    }


    public Link(float propagationDelay, float bw, EventsQueue eventsQueue){
        segmentsFromA = new ArrayList<>();
        segmentsFromB = new ArrayList<>();
        this.propagationDelay = propagationDelay;
        this.bw = bw;
        this.eventsQueue = eventsQueue;
    }

    public Link(EndDevice endPointA, EndDevice endPointB, float propagationDelay, float bw, int weight, EventsQueue eventsQueue) {
        this(propagationDelay, bw, eventsQueue);
        this.endPointA = endPointA;
        this.endPointB = endPointB;
        this.weight = weight;
    }

    public float getPropagationDelay() {
        return propagationDelay;
    }

    public void setPropagationDelay(float propagationDelay) {
        this.propagationDelay = propagationDelay;
    }

    public float getBw() {
        return bw;
    }

    public void setBw(float bw) {
        this.bw = bw;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public EndDevice getEndPointA() {
        return endPointA;
    }

    public void setEndPointA(EndDevice endPointA) {
        this.endPointA = endPointA;
    }

    public EndDevice getEndPointB() {
        return endPointB;
    }

    public void setEndPointB(EndDevice endPointB) {
        this.endPointB = endPointB;
    }
//    public static double totalTimeInLinkHandleEvent = 0;

    @Override
    public void handleEvent(Event event) throws Exception {
//        double tempTime = System.currentTimeMillis();

        if(!event.getRelatedEntity().equals(this)){
            throw new Exception("");
        }

        if(event.getType() == EventType.sendData){
            if(event.getCreator().equals(endPointA))
                segmentsFromA.add((Segment) event.getOptionalData());
            else if (event.getCreator().equals(endPointB)) segmentsFromB.add((Segment) event.getOptionalData());
            else throw new Exception("Illegal received segment.");
//            System.out.println(event.getTime()+" "+(Segment) event.getOptionalData() +"is being sent by " + this);
            checkForSendData(event.getTime());
        }
        else if(event.getType() == EventType.dataSent){
            //Remove sent segment from queue
            Segment sentSegment = (Segment) event.getOptionalData();
            sentSegment.increaseToleratedCost(this.weight);
            boolean isInA = segmentsFromA.remove(sentSegment);
            boolean isInB = segmentsFromB.remove(sentSegment);

            if(!isInA && !isInB)
                throw new Exception(sentSegment+ " not found in " + this);

            if(isInA)
                isSendingFromA = false;
            else isSendingFromB = false;

            //Create Event for receiver
            Event e = new Event<>(EventType.receiveSegment,
                    (isInA)? endPointB : endPointA, event.getTime(), this, sentSegment );
            eventsQueue.addEvent(e);
//            System.out.println(event.getTime()+" "+(Segment) event.getOptionalData() + " sent by " + this);
            //SendNextSegment
            checkForSendData(event.getTime());
        }
//        totalTimeInLinkHandleEvent +=System.currentTimeMillis()-tempTime;

    }
    private void checkForSendData(float time) {

        if(segmentsFromA.size()>0 && !isSendingFromA){
            float eventTime =DefaultValues.LINK_DELAY_ALLOWED? time + propagationDelay + segmentsFromA.get(0).getSize()/bw:time;
            Event<Link> event = new Event<>(EventType.dataSent, this, eventTime, this, segmentsFromA.get(0));
            eventsQueue.addEvent(event);

            isSendingFromA = true;
        }
        else if(segmentsFromB.size()> 0 && !isSendingFromB){
            float eventTime = DefaultValues.LINK_DELAY_ALLOWED? time + propagationDelay + segmentsFromB.get(0).getSize()/bw:time;
            Event<Link> event = new Event<>(EventType.dataSent, this, eventTime, this, segmentsFromB.get(0));
            eventsQueue.addEvent(event);

            isSendingFromB = true;
        }
    }

    public EndDevice getOtherEndPoint(EndDevice endDevice){
        return endDevice.equals(endPointA)?endPointB:endPointB;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Link{");
        sb.append("endPointA=").append(endPointA.toString());
        sb.append(", endPointB=").append(endPointB.toString());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(endPointA, link.endPointA) &&
                Objects.equals(endPointB, link.endPointB);
    }

    @Override
    public int hashCode() {

        return Objects.hash(endPointA, endPointB);
    }
}
