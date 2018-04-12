package camera.hj.cameracontroller.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import butterknife.ButterKnife;
import camera.hj.cameracontroller.R;
import camera.hj.cameracontroller.ui.fragment.MyTrainFragment;
import camera.hj.cameracontroller.ui.fragment.SingleTrainFragment;
import camera.hj.cameracontroller.ui.fragment.TrainPlanFragment;
import camera.hj.cameracontroller.utils.AppManager;

/**
 * Created by NC040 on 2017/12/21.
 */

public class MainPageActivity extends FragmentActivity {

    private FragmentTabHost mTabHost;

    private Class[] mFragmentArrays = {SingleTrainFragment.class,TrainPlanFragment.class,MyTrainFragment.class};

    private String[] mTextArrays = {"单项训练", "健身计划", "我的"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        setContentView(R.layout.activity_mainpage);
        ButterKnife.bind(this);
        initToolBar();
        setTabHost();
    }

    private void initToolBar() {
        getActionBar().setDisplayHomeAsUpEnabled(false);// 禁止显示左箭头
        getActionBar().setCustomView(LayoutInflater.from(this).inflate(R.layout.mainpage_title, null));
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
    }


    private void setTabHost() {
            mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
            mTabHost.setup(this, getSupportFragmentManager(), R.id.content_frame);

            int count = 0;
            count = mFragmentArrays.length;
            for (int i = 0; i < count; i++) {
                //cancel the divided line among tabhosts
                mTabHost.getTabWidget().setDividerDrawable(null);

                // 将Tab按钮添加进Tab选项卡中
                // 给每个Tab按钮设置图标、文字和内容
                TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextArrays[i])
                        .setIndicator(getTabItemView(i));
                mTabHost.addTab(tabSpec, mFragmentArrays[i], null);
                mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 100;
                //设置Tab按钮背景
                mTabHost.getTabWidget().getChildAt(i)
                        .setBackgroundResource(R.drawable.selector_tab_background);

            }

    }

    private View getTabItemView(int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_layout, null);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextArrays[index]);
        return view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().removeActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
