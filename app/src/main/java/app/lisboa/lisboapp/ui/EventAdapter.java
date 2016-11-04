package app.lisboa.lisboapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import app.lisboa.lisboapp.R;
import app.lisboa.lisboapp.model.Event;
import app.lisboa.lisboapp.utils.FundaTextView;

/**
 * Created by Rajat Anantharam on 02/11/16.
 */
public class EventAdapter extends ArrayAdapter<Event> {

    private Context mContext;
    private int resourceViewId;
    private String name, location, startTime, duration;
    private FirebaseUser firebaseUser;
    private OnJoinedRoomListener joinedRoomListener;


    public EventAdapter(Context context, int resource, List<Event> objects, FirebaseUser firebaseUser) {
        super(context, resource, objects);
        this.mContext = context;
        this.resourceViewId = resource;
        this.firebaseUser = firebaseUser;
    }

    private UIContainer holder;

    public void setJoinedRoomListener(OnJoinedRoomListener joinedRoomListener) {
        this.joinedRoomListener = joinedRoomListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resourceViewId, parent, false);
            holder = new UIContainer(convertView);
            convertView.setTag(holder);
        } else {
            holder = (UIContainer) convertView.getTag();
        }

        final Event event = getItem(position);
        if(event!=null) {
            name = event.eventName + " with " + event.hostName;
            int count = event.attendees!=null ? event.attendees.size() : 0 ;
            location = "+ " + count + " others at "+ event.locationName;

            if(event.attendees!=null && event.attendees.contains(firebaseUser.getUid())) {
                holder.joinRoom.setSelected(true);
            } else {
                holder.joinRoom.setSelected(false);
            }
            Date date = new Date(event.startTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getDefault());

            startTime = String.valueOf(simpleDateFormat.format(date));
            duration = String.valueOf(event.durationInMinutes) + "m";

            holder.eventName.setText(name);
            holder.eventLocation.setText(location);
            holder.eventTime.setText(startTime);
            holder.eventDuration.setText(duration);
            holder.eventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view == null) {
                        return;
                    }
                    joinedRoomListener.onJoinedRoom(event, view);
                }
            });
        }

        return convertView;
    }

    private class UIContainer {
        public ViewGroup eventView;
        public FundaTextView eventName, eventLocation, eventTime, eventDuration;
        public Button joinRoom;

        public UIContainer(View convertView) {
            eventView = (ViewGroup) convertView.findViewById(R.id.eventView);
            eventName = (FundaTextView) convertView.findViewById(R.id.eventName);
            eventLocation = (FundaTextView) convertView.findViewById(R.id.locationName);
            eventTime = (FundaTextView) convertView.findViewById(R.id.eventTime);
            eventDuration = (FundaTextView) convertView.findViewById(R.id.eventDuration);
            joinRoom = (Button) convertView.findViewById(R.id.joinRoom);
        }
    }
}