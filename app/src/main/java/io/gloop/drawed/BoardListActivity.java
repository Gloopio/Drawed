package io.gloop.drawed;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopLogger;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.dialogs.AcceptBoardAccessDialog;
import io.gloop.drawed.dialogs.NewBoardDialog;
import io.gloop.drawed.dialogs.SearchDialog;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.model.UserInfo;
import io.gloop.drawed.utils.NotificationUtil;
import io.gloop.drawed.utils.SharedPreferencesStore;
import io.gloop.permissions.GloopUser;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BoardDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BoardListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;
    //    private RecyclerView recyclerView;
//    private BoardAdapter boardAdapter;
    private DrawerLayout mDrawerLayout;

    private CircleImageView userImage;
    private TextView username;


    private GloopUser owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

//        recyclerView = (RecyclerView) findViewById(R.id.item_list);

//        //set username
        username = (TextView) findViewById(R.id.username);

        // Load the currently logged in GloopUser of the app.
        this.owner = Gloop.getOwner();
//        if (owner != null) {
//            String name = this.owner.getName();
//            if (name != null)
//                username.setText(name);
//        }


        LinearLayout header = (LinearLayout) findViewById(R.id.header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDiag();
            }
        });
        userImage = (CircleImageView) findViewById(R.id.user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDiag();
            }
        });


        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

        final FloatingActionMenu floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fab_menu_item_search);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchDialog(BoardListActivity.this, owner, mTwoPane, BoardListActivity.this.getSupportFragmentManager()).show();
                floatingActionMenu.close(false);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_menu_item_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NewBoardDialog(BoardListActivity.this, owner, view, mTwoPane, BoardListActivity.this.getSupportFragmentManager()).show();
                floatingActionMenu.close(false);
            }
        });

        FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.fab_menu_item_scan);
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchScanner(QRCodeScannerActivity.class);
                floatingActionMenu.close(false);
            }
        });

        // TODO
//        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Gloop.sync();
//                        checkForPrivateBoardAccessRequests();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mSwipeRefreshLayout.setRefreshing(false);
//                            }
//                        });
//                    }
//                }).start();
//            }
//        });

//        LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
//        footer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new UserDialog(BoardListActivity.this, owner).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                menu.findItem(R.id.menu_night_mode_system).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                menu.findItem(R.id.menu_night_mode_auto).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                menu.findItem(R.id.menu_night_mode_night).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                menu.findItem(R.id.menu_night_mode_day).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_night_mode_system:
                setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case R.id.menu_night_mode_day:
                setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.menu_night_mode_night:
                setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.menu_night_mode_auto:
                setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logout:
                logout();
                break;
        }

        item.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void logout() {
        Gloop.logout();
        SharedPreferencesStore.clearUser();
        finish();
    }


    private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);

        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new ListFragment(), "Category 1");
        adapter.addFragment(new ListFragment(), "Category 2");
        adapter.addFragment(new ListFragment(), "Category 3");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private Class<?> mClss;


    public void launchScanner(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZBAR_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mClss != null) {
                        Intent intent = new Intent(this, mClss);
//                        intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        setupRecyclerView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                setUserInfo();
                checkForPrivateBoardAccessRequests();
            }
        }).start();
    }

    private void setUserInfo() {
        final UserInfo userInfo = Gloop.allLocal(UserInfo.class)
                .where()
                .equalsTo("email", owner.getName())
                .first();

        if (userInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(getApplicationContext())
                            .load(userInfo.getImageURL())
//                            .resize(80, 80)
//                            .centerCrop()
                            .into(userImage);

                    username.setText(userInfo.getUserName());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        setupRecyclerView();
        checkForPrivateBoardAccessRequests();
    }

    @Override
    public void onStop() {
        super.onStop();
        SaveInBackgroundWorker.getInstance().stopWorker();
    }

    @Override
    public void onPause() {
        super.onPause();
        SaveInBackgroundWorker.getInstance().stopWorker();
    }


    private void checkForPrivateBoardAccessRequests() {
        final GloopList<BoardAccessRequest> accessRequests = Gloop
                .all(BoardAccessRequest.class)
                .where()
                .equalsTo("boardCreator", owner.getUserId())
                .all();
        for (BoardAccessRequest accessRequest : accessRequests) {
            NotificationUtil.show(BoardListActivity.this, accessRequest);
        }

        accessRequests.addOnChangeListener(new GloopOnChangeListener() {
            @Override
            public void onChange() {
                GloopLogger.i("Request access to a private board");
                GloopLogger.i(accessRequests);
                for (BoardAccessRequest accessRequest : accessRequests) {
//                    showNotification(accessRequest);
                    new AcceptBoardAccessDialog(BoardListActivity.this, accessRequest);
                }
            }
        });
    }


//    private void setupRecyclerView() {
//        // Load all locally saved boards to the boards list.
//        GloopList<Board> boards = Gloop.allLocal(Board.class);
//
//        boardAdapter = new BoardAdapter(boards);
//        recyclerView.setAdapter(boardAdapter);
//    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private final int SELECT_PHOTO = 1;


    private void showDiag() {

        final View dialogView = View.inflate(this, R.layout.dialog, null);

        final Dialog dialog = new Dialog(this, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        Button button = (Button) dialog.findViewById(R.id.image_chooser);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
//                PickImageDialog.build(new PickSetup())
//                        .setOnPickResult(new IPickResult() {
//                            @Override
//                            public void onPickResult(PickResult r) {
//                                if (r.getError() == null) {
//                                    //If you want the Uri.
//                                    //Mandatory to refresh image from Uri.
//                                    //getImageView().setImageURI(null);
//
//                                    //Setting the real returned image.
//                                    //getImageView().setImageURI(r.getUri());
//
//                                    //If you want the Bitmap.
////                                    getImageView().setImageBitmap(r.getBitmap());
//                                    userImage.setImageBitmap(r.getBitmap());
//                                    revealShow(dialogView, false, dialog);
//
//                                    //Image path
//                                    //r.getPath();
//                                } else {
//                                    //Handle possible errors
//                                    //TODO: do what you have to do with r.getError();
//                                    Toast.makeText(getApplicationContext(), r.getError().getMessage(), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }).show(BoardListActivity.this);
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.closeDialogImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                revealShow(dialogView, false, dialog);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                revealShow(dialogView, true, null);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {

                    revealShow(dialogView, false, dialog);
                    return true;
                }

                return false;
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                // Should we show an explanation?
                                if (shouldShowRequestPermissionRationale(
                                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    // Explain to the user why we need to read the contacts
                                }

                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                                // app-defined int constant that should be quite unique

                                return;
                            }
                        }

                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage = ThumbnailUtils.extractThumbnail(selectedImage, 200, 200);
                        userImage.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }


    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.dialog);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = (int) (userImage.getX() + (userImage.getWidth() / 2));
        int cy = (int) (userImage.getY()) + userImage.getHeight() + 56;


        if (b) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(700);
            revealAnimator.start();

        } else {

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(700);
            anim.start();
        }

    }


}