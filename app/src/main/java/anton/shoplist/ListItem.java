package anton.shoplist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anton on 15.05.15.
 */
public class ListItem implements Parcelable {

    private String name;
    private boolean isChecked;

    ListItem(String name) {
        this.name = name;
        isChecked = false;
    }

    ListItem(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    private ListItem(Parcel in) {
        this.name = in.readString();
        this.isChecked = in.readByte() != 0;
    }

    public boolean checkItem() {
        return this.isChecked;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return this.name;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeByte((byte) (this.isChecked ? 1 : 0));
    }

    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };
}
