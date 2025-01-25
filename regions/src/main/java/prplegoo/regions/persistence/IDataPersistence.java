package prplegoo.regions.persistence;

public interface IDataPersistence<T> {
    String getKey();

    T getData();

    void putData(T data);
}
