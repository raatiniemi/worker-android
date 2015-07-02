package me.raatiniemi.worker.project.timesheet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.List;

import me.raatiniemi.worker.R;
import me.raatiniemi.worker.base.view.MvpFragment;
import me.raatiniemi.worker.mapper.MapperRegistry;
import me.raatiniemi.worker.mapper.TimeMapper;
import me.raatiniemi.worker.model.project.ProjectProvider;
import me.raatiniemi.worker.model.time.Time;
import me.raatiniemi.worker.project.timesheet.TimesheetAdapter.TimeInAdapterResult;
import me.raatiniemi.worker.projects.ProjectsFragment;
import me.raatiniemi.worker.util.TimesheetExpandableDataProvider;
import me.raatiniemi.worker.util.TimesheetExpandableDataProvider.Groupable;
import me.raatiniemi.worker.util.TimesheetExpandableDataProvider.TimeChild;

public class TimesheetFragment extends MvpFragment<TimesheetPresenter, List<Groupable>>
    implements TimesheetAdapter.OnTimesheetListener, TimesheetView {
    private static final String TAG = "TimesheetFragment";

    private LinearLayoutManager mLinearLayoutManager;

    private RecyclerView mRecyclerView;

    private TimesheetExpandableDataProvider mProvider;

    private TimesheetAdapter mTimesheetAdapter;

    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    private TimeInAdapterResult mSelectedItem;

    private View mSelectedView;

    private ActionMode mActionMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            // TODO: Retrieve title from resources, for localization support.
            actionMode.setTitle("Actions");
            actionMode.getMenuInflater().inflate(R.menu.actions_project, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
            boolean finish = false;

            switch (item.getItemId()) {
                case R.id.actions_project_delete:
                    getPresenter().remove(mSelectedItem);
                    finish = true;
                    break;
                default:
                    Log.w(TAG, "Undefined action: " + item.getItemId());
                    break;
            }

            mSelectedItem = null;
            if (finish) {
                actionMode.finish();
            }
            return finish;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            // If there's a selected row, we have
            // to clear the selection-state.
            if (null != mSelectedView) {
                mSelectedView.setSelected(false);
                mSelectedView = null;
            }

            mActionMode = null;
        }
    };

    private boolean mLoading = false;

    private long getProjectId() {
        return getArguments().getLong(ProjectsFragment.MESSAGE_PROJECT_ID, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timesheet, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProvider = new TimesheetExpandableDataProvider();

        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        mTimesheetAdapter = new TimesheetAdapter(mProvider, this);
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_timesheet);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mRecyclerViewExpandableItemManager.createWrappedAdapter(mTimesheetAdapter));
        mRecyclerView.addItemDecoration(
            new SimpleListDividerDecorator(
                getResources().getDrawable(R.drawable.expandable_list_item_divider),
                true
            )
        );
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // Make sure we're not loading data before checking the position.
                if (!mLoading) {
                    // Retrieve positional data, needed for the calculation on whether
                    // we are close to the end of the list or not.
                    int visibleItems = mLinearLayoutManager.getChildCount();
                    int firstVisiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                    // Retrieve the total number of items within the recycler view,
                    // this will include both the group and the children.
                    int totalItems = mLinearLayoutManager.getItemCount();

                    // Check if the last row in the list is visible.
                    if ((visibleItems + firstVisiblePosition) >= totalItems) {
                        // We are about to start loading data, and thus we need
                        // to block additional loading requests.
                        mLoading = true;

                        // Retrieve the total number of groups within the view, we need to
                        // exclude the children otherwise the offset will be wrong.
                        int offset = mTimesheetAdapter.getGroupCount();

                        // Retrieve additional timesheet items with offset.
                        getPresenter().getTimesheet(getProjectId(), offset);
                    }
                }
            }
        });
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        getPresenter().attachView(this);
        getPresenter().getTimesheet(getProjectId(), 0);
    }

    @Override
    protected TimesheetPresenter createPresenter() {
        return new TimesheetPresenter(getActivity(), new ProjectProvider(getActivity()));
    }

    @Override
    public List<Groupable> getData() {
        return null;
    }

    @Override
    public void setData(List<Groupable> data) {
    }

    public void addData(List<Groupable> data) {
        if (0 == data.size()) {
            Log.d(TAG, "No data to add");
            return;
        }

        for (Groupable group : data) {
            mTimesheetAdapter.addGroup(group);
        }

        // If we are adding addional data we have to unlock the loading,
        // otherwise additional data will not be loaded.
        mLoading = false;
    }

    public void remove(TimeInAdapterResult result) {
        mTimesheetAdapter.remove(result.getGroup(), result.getChild());
    }

    @Override
    public boolean onTimeLongClick(View view, TimeInAdapterResult result) {
        mSelectedItem = result;

        // If there's already a selected row, we have
        // to clear the selection-state.
        if (null != mSelectedView) {
            mSelectedView.setSelected(false);
        }

        // Save the view for reference and put
        // the view in the selection-state.
        mSelectedView = view;
        mSelectedView.setSelected(true);

        // Only start the ActionMode if none has already started.
        if (null == mActionMode) {
            mActionMode = getActivity().startActionMode(mActionModeCallback);
        }
        return true;
    }
}
