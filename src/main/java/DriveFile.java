import com.google.api.client.util.DateTime;

public class DriveFile {
    String Id;
    String Name;
    String Type;
    DateTime LastModifiedDate;

    public DriveFile(String id, String name) {
        Id = id;
        Name = name;
    }
    public DriveFile(String id, String name, String type, DateTime lastModifiedDate) {
        Id = id;
        Name = name;
        Type = type;
        LastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return "{id: "+Id+",Name: "+Name+",Type: "+Type+",LastModifiedDate: "+LastModifiedDate+"}";
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getType() {
        return Type;
    }

    public DateTime getLastModifiedDate() {
        return LastModifiedDate;
    }
}
