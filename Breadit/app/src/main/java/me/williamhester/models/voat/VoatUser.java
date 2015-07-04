package me.williamhester.models.voat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.bulletin.User;

/**
 * Created by william on 7/4/15.
 */
public class VoatUser implements User {

    private String userName;
    private String registrationDate;
    private String bio;
    private String profilePicture;
    private Points commentPoints;
    private Points submissionPoints;
    private Points commentVoting;
    private Points submissionVoting;
    private List<Badge> badges;

    public VoatUser() {
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getCreatedDate() {
        return registrationDate;
    }

    @Override
    public int getCommentPoints() {
        return commentPoints.sum;
    }

    @Override
    public int getSubmissionPoints() {
        return submissionPoints.sum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userName);
        dest.writeString(this.registrationDate);
        dest.writeString(this.bio);
        dest.writeString(this.profilePicture);
        dest.writeParcelable(this.commentPoints, flags);
        dest.writeParcelable(this.submissionPoints, flags);
        dest.writeParcelable(this.commentVoting, flags);
        dest.writeParcelable(this.submissionVoting, flags);
        dest.writeList(this.badges);
    }

    protected VoatUser(Parcel in) {
        this.userName = in.readString();
        this.registrationDate = in.readString();
        this.bio = in.readString();
        this.profilePicture = in.readString();
        this.commentPoints = in.readParcelable(Points.class.getClassLoader());
        this.submissionPoints = in.readParcelable(Points.class.getClassLoader());
        this.commentVoting = in.readParcelable(Points.class.getClassLoader());
        this.submissionVoting = in.readParcelable(Points.class.getClassLoader());
        this.badges = new ArrayList<>();
        in.readList(this.badges, List.class.getClassLoader());
    }

    public static final Creator<VoatUser> CREATOR = new Creator<VoatUser>() {
        public VoatUser createFromParcel(Parcel source) {
            return new VoatUser(source);
        }

        public VoatUser[] newArray(int size) {
            return new VoatUser[size];
        }
    };

    public static class Points implements Parcelable {
        private int upCount;
        private int downCount;
        private int sum;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.upCount);
            dest.writeInt(this.downCount);
            dest.writeInt(this.sum);
        }

        public Points() {
        }

        protected Points(Parcel in) {
            this.upCount = in.readInt();
            this.downCount = in.readInt();
            this.sum = in.readInt();
        }

        public static final Creator<Points> CREATOR = new Creator<Points>() {
            public Points createFromParcel(Parcel source) {
                return new Points(source);
            }

            public Points[] newArray(int size) {
                return new Points[size];
            }
        };
    }

    public static class Badge implements Parcelable {
        private String name;
        private String awardedDate;
        private String title;
        private String badgeGraphic;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.awardedDate);
            dest.writeString(this.title);
            dest.writeString(this.badgeGraphic);
        }

        public Badge() {
        }

        protected Badge(Parcel in) {
            this.name = in.readString();
            this.awardedDate = in.readString();
            this.title = in.readString();
            this.badgeGraphic = in.readString();
        }

        public static final Creator<Badge> CREATOR = new Creator<Badge>() {
            public Badge createFromParcel(Parcel source) {
                return new Badge(source);
            }

            public Badge[] newArray(int size) {
                return new Badge[size];
            }
        };
    }
}
