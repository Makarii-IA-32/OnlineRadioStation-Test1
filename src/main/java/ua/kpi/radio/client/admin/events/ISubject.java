package ua.kpi.radio.client.admin.events;

public interface ISubject {
    void attach(IObserver observer);
    void detach(IObserver observer);
    void notifyObservers(int playlistId);
}