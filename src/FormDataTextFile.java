
public class FormDataTextFile {
    public String formName;
    public String fileName;
    public String contentType;
    public String body;

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFormName() {
        return formName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBody() {
        return body;
    }
}
