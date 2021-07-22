package priv.zxw.dictranslate.entity;

public final class DictionaryEntity {
    private Long id;
    private Integer type;
    private String value;

    public DictionaryEntity() {}

    public DictionaryEntity(Integer type, Long id) {
        this.id = id;
        this.type = type;
    }

    public DictionaryEntity(Integer type, Long id, String value) {
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
