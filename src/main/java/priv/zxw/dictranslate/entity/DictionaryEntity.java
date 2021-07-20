package priv.zxw.dictranslate.entity;

public final class DictionaryEntity {
    private Long id;
    private Integer type;
    private String value;

    public DictionaryEntity() {}

    public DictionaryEntity(Long id, Integer type) {
        this.id = id;
        this.type = type;
    }

    public DictionaryEntity(Long id, Integer type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
