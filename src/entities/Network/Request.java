/*
 * Developed By Saeed Hadadan, INL lab, Sharif University of Technology: www.inl-lab.net
 * Copyright (c) 2019. All rights reserved.
 *
 */

package entities.Network;

import entities.Network.Client;
import entities.Network.Server;

public class Request {
    private Client source;
    private Server destination;
    private Server serverToPiggyBack;
    private int neededFileID;
    private final int id;     //This must be the same id in segment
    private boolean isRedirect = false;
    private boolean shouldBePiggiedBack = false;
    private int toleratedCost =0;
    public boolean isRedirected() {
        return isRedirect;
    }

    public void setRedirect(boolean redirect) {
        isRedirect = redirect;
    }

    public int getNeededFileID() {
        return neededFileID;
    }

    public Request(Client source, Server destination, int neededFileID , int id) {
        this.source = source;
        this.destination = destination;
        this.neededFileID = neededFileID;
        this.id = id;
    }

    public void setNeededFileID(int neededFileID) {
        this.neededFileID = neededFileID;
    }

    public Client getSource() {
        return source;
    }

    public void setSource(Client source) {
        this.source = source;
    }

    public Server getDestination() {
        return destination;
    }

    public void setDestination(Server destination) {
        this.destination = destination;
    }

    public int getId() {
        return id;
    }

    public void setToleratedCost(int toleratedCost) {
        this.toleratedCost = toleratedCost;
    }

    public int getToleratedCost() {
        return toleratedCost;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Request{");
        sb.append(id);
        sb.append('}');
        return sb.toString();
    }

    public Server getServerToPiggyBack() {
        return serverToPiggyBack;
    }

    public void setServerToPiggyBack(Server serverToPiggyBack) {
        this.serverToPiggyBack = serverToPiggyBack;
    }

    public boolean getShouldBePiggiedBack() {
        return shouldBePiggiedBack;
    }

    public void setShouldBePiggiedBack(boolean shouldBePiggiedBack) {
        this.shouldBePiggiedBack = shouldBePiggiedBack;
    }
}
