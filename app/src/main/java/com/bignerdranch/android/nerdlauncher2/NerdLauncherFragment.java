package com.bignerdranch.android.nerdlauncher2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by My on 1/27/2016.
 */
public class NerdLauncherFragment extends Fragment {
   private RecyclerView    mRecyclerView;
   private static final String   TAG = "NerdLauncherFragment";

   public static NerdLauncherFragment newInstance() {
      return new NerdLauncherFragment();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
      mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_nerd_launcher_recycler_view);
      mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
      setupAdapter();

      return view;
   }

   private void setupAdapter() {
      Intent startupIntent = new Intent(Intent.ACTION_MAIN);
      startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      final PackageManager pm = getActivity().getPackageManager();
      // query the system (using the PackageManager) for launchable main activities, which have
      // intent filters that include a MAIN action and a LAUNCHER category. (a launchable app is an
      // app the user can open by clicking an icon on the Home or launcher screen)
      List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
      // sort the ResolveInfo objects returned from the PackageManager alphabetically by label using
      // ResolveInfo.loadLabel()
      Collections.sort(activities, new Comparator<ResolveInfo>() {
         @Override
         public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            return String.CASE_INSENSITIVE_ORDER.compare(lhs.loadLabel(pm).toString(), rhs.loadLabel(pm).toString());
         }
      });
      Log.i(TAG, "Found " + activities.size() + " activities");
      // create an instance of ActivityAdapter and set it as the RecyclerView's adapter
      mRecyclerView.setAdapter(new ActivityAdapter(activities));
   }

   private class ActivityHolder extends RecyclerView.ViewHolder {
      private ResolveInfo  mResolveInfo;
      private TextView     mNameTextView;

      public ActivityHolder(View view) {
         super(view);
         mNameTextView = (TextView)view;
         mNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ActivityInfo activityInfo = mResolveInfo.activityInfo;
               // create an explicit Intent
               Intent intent = new Intent(Intent.ACTION_MAIN)
                     // based on the activity's package name and class name extracted from
                     // ResolveInfo.ActiveInfo
                     .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                     // force activity to start in its own task and not the NerdLauncher's task, as
                     // shown in the overview screen
                     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               // use the explicit Intent to launch the selected activity
               startActivity(intent);
            }
         });
      }

      public void bindActivity(ResolveInfo resolveInfo) {
         mResolveInfo = resolveInfo;
         PackageManager pm = getActivity().getPackageManager();
         // extract the app's label from resolveInfo
         String appName = mResolveInfo.loadLabel(pm).toString();
         mNameTextView.setText(appName);
      }
   }

   private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {
      private List<ResolveInfo>  mActivities;

      public ActivityAdapter(List<ResolveInfo> activities) {
         mActivities = activities;
      }

      @Override
      public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         LayoutInflater inflater = LayoutInflater.from(getActivity());
         View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
         return new ActivityHolder(view);
      }

      @Override
      public void onBindViewHolder(ActivityHolder holder, int position) {
         ResolveInfo resolveInfo = mActivities.get(position);
         holder.bindActivity(resolveInfo);
      }

      @Override
      public int getItemCount() {
         return mActivities.size();
      }
   }
}
