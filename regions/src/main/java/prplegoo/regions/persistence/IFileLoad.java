package prplegoo.regions.persistence;

public interface IFileLoad{
    <T> T get(IDataPersistence<T> dataPersistence);
}
