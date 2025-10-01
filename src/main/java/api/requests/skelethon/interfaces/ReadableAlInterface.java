package api.requests.skelethon.interfaces;

public interface ReadableAlInterface {
    Object get();
    Object getAllById(long id);
    Object getAll(Class<?> clazz);
}
