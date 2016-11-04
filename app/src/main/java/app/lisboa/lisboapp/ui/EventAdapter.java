package app.lisboa.lisboapp.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

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
    private FirebaseUser firebaseUser;


    public EventAdapter(Context context, int resource, List<Event> objects, FirebaseUser firebaseUser) {
        super(context, resource, objects);
        this.mContext = context;
        this.resourceViewId = resource;
        this.firebaseUser = firebaseUser;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        UIContainer holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resourceViewId, parent, false);
            holder = new UIContainer(convertView);
            convertView.setTag(holder);
        } else {
            holder = (UIContainer) convertView.getTag();
        }

        final Event event = getItem(position);
        if(event!=null) {

            String name = event.eventName + " with " + event.hostName;
            int count = event.attendees!=null ? event.attendees.size() : 0 ;
            String others = "others";
            if(count== 1) {
                others = "other";
            }
            String location = "+ " + count + " " + others + " at " + event.locationName;

            if(event.attendees!=null && event.attendees.contains(firebaseUser.getUid())) {
                holder.eventJoinButton.setSelected(true);
            } else {
                holder.eventJoinButton.setSelected(false);
            }

            long now = System.currentTimeMillis()/1000L ;
            if(now > (event.startTime + event.durationInMinutes * 60)) {
                holder.eventParentView.setAlpha(0.3f);
                holder.eventParentView.setClickable(false);
                holder.eventJoinButton.setClickable(false);
            } else {
                holder.eventParentView.setAlpha(1f);
                holder.eventJoinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).onJoinButtonClicked(event);
                    }
                });
                holder.eventParentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity)mContext).onListItemClicked(event);
                    }
                });
            }

            Date date = new java.util.Date(event.startTime * 1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getDefault());

            String eventTime = String.valueOf(simpleDateFormat.format(date));
            String duration = String.valueOf(event.durationInMinutes) + "m";

            holder.eventNameTextView.setText(name);
            holder.eventLocationTextView.setText(location);
            holder.eventTimeTextView.setText(eventTime);
            holder.eventDurationTextView.setText(duration);
        }

        return convertView;
    }

    private class UIContainer {
        ViewGroup eventParentView;
        FundaTextView eventNameTextView, eventLocationTextView, eventTimeTextView, eventDurationTextView;
        Button eventJoinButton;
        UIContainer(View convertView) {
            eventParentView = (ViewGroup) convertView.findViewById(R.id.eventView);
            eventNameTextView = (FundaTextView) convertView.findViewById(R.id.eventName);
            eventLocationTextView = (FundaTextView) convertView.findViewById(R.id.locationName);
            eventTimeTextView = (FundaTextView) convertView.findViewById(R.id.eventTime);
            eventDurationTextView = (FundaTextView) convertView.findViewById(R.id.eventDuration);
            eventJoinButton = (Button) convertView.findViewById(R.id.joinRoom);
        }
    }
}