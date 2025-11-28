package ua.kpi.radio.client.admin.events;

import java.util.ArrayList;
import java.util.List;

public class EventManager implements ISubject {

    private static final EventManager INSTANCE = new EventManager();
    private final List<IObserver> observers = new ArrayList<>();

    private EventManager() {}

    public static EventManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void attach(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(int playlistId) {
        for (IObserver observer : observers) {
            observer.update(playlistId);
        }
    }
}