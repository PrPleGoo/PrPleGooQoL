package prplegoo.regions.persistence;

public interface IFileLoad{
    <T> T get(String key, Class<T> type);
}
