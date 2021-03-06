package com.shenma.tvlauncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalHttp;

import com.android.volley.Request.Method;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lisen.Encoder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shenma.tvlauncher.adapter.UserTypeAdapter;
import com.shenma.tvlauncher.dao.bean.AppInfo;
import com.shenma.tvlauncher.db.DatabaseOperator;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.tvlive.TVLivePlayer;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.WiFiDialog;
import com.shenma.tvlauncher.vod.VodDetailsActivity;
import com.shenma.tvlauncher.vod.dao.VodDao;
import com.shenma.tvlauncher.vod.db.Album;
import com.umeng.analytics.MobclickAgent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * @Description ????????????
 *
 * @author joychang
 *
 */
public class UserActivity extends BaseActivity {
	
	
	private RadioGroup rg_member;
	private RadioButton rb_user;
	private RadioButton rb_user_alert;
	private RadioButton rb_user_history;
	private RadioButton rb_user_app;
	private RadioButton rb_user_collect;
	private TextView tv_no_data,tv_filter_content;
	private LinearLayout user_type_details;
	private GridView gv_user_type_details_grid;
	private List<Album> Albumls = null;
	private UserTypeAdapter userTypeAdapter = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private VodDao dao;
	private int mPosition = -1;
	private PopupWindow menupopupWindow;
	private ListView menulist;
	private MyAdapter mAdapter = null;
	private int USER_TYPE;
	private DatabaseOperator dbtools;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_member);
		findViewById(R.id.member).setBackgroundResource(R.drawable.video_details_bg);
		dao = new VodDao(this);
		dbtools = new DatabaseOperator(this);
        mQueue = Volley.newRequestQueue(this, new HurlStack());
		initView();
		initData();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (fromApp) {
			//??????????????????
//			if (userTypeAdapter == null) {
//				userTypeAdapter = new UserTypeAdapter(UserActivity.this, queryAllOftenApps(), imageLoader,ISAPP);
//				gv_user_type_details_grid.setAdapter(userTypeAdapter);
//			} else {
//				if (null != templovLst && templovLst.size() > 0) {
//					userTypeAdapter.setAppData(queryAllOftenApps());
//					userTypeAdapter.notifyDataSetChanged();
//				} else {
					setOftenApp();
//				}
				
//			}
		}
		Logger.d(TAG, "onStart()...");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Logger.d(TAG, "onPause()...");
		MobclickAgent.onPageEnd(TAG);
		MobclickAgent.onPause(this);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.d(TAG, "onResume()...");
		MobclickAgent.onPageStart(TAG);
		MobclickAgent.onResume(this);
	}

	@Override
	protected void initView() {
		loadViewLayout();
		findViewById();
		setListener();
	}
	
	/**
	 * ???????????????
	 */
	private void initData(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				queryAllOftenApps();//?????????????????????app
			}
		}).start();
		String uName = sp.getString("userName", null);
		if(!TextUtils.isEmpty(uName)) {
			tv_user_name.setText(uName);
			tv_no_data.setText("??????????????????");
		}else{
			tv_no_data.setText("?????????????????????~");
		}
		tv_no_data.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			if(!ISAPP){
				showMenu();
			}else{
				openActivity(AppManageActivity.class);
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			hideMenu();
			break;

		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void loadViewLayout() {
		onCreateMenu();
	}

	@Override
	protected void findViewById() {
		tv_user_name = (TextView) findViewById(R.id.tv_user_name);
		rg_member = (RadioGroup) findViewById(R.id.rg_member);
		rb_user = (RadioButton) findViewById(R.id.rb_user);
		rb_user_alert = (RadioButton) findViewById(R.id.rb_user_alert);
		rb_user_history = (RadioButton) findViewById(R.id.rb_user_history);
		rb_user_app = (RadioButton) findViewById(R.id.rb_user_app);
		rb_user_collect = (RadioButton) findViewById(R.id.rb_user_collect);
		gv_user_type_details_grid = (GridView) findViewById(R.id.user_type_details_grid);
		gv_user_type_details_grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		user_type_details = (LinearLayout) findViewById(R.id.user_type_details);
		tv_no_data = (TextView) findViewById(R.id.tv_no_data);
		tv_filter_content = (TextView) findViewById(R.id.filter_content);
		rb_user.setChecked(true);
	}
	
	private void setOftenApp(){
		tv_filter_content.setVisibility(View.GONE);
		queryAllOftenApps();
		if(null!=templovLst&&templovLst.size()>0){
			tv_no_data.setVisibility(View.GONE);
			gv_user_type_details_grid.setVisibility(View.VISIBLE);
			userTypeAdapter = new UserTypeAdapter(UserActivity.this, templovLst, imageLoader,ISAPP);
			gv_user_type_details_grid.setAdapter(userTypeAdapter);
		}else{
			tv_no_data.setVisibility(View.VISIBLE);
			gv_user_type_details_grid.setVisibility(View.GONE);
			if(null!=userTypeAdapter){
				userTypeAdapter.clearDatas();
				userTypeAdapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	protected void setListener() {
		rg_member.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup rg, int position) {
			}
		});
		
		for (int i = 0; i < rg_member.getChildCount(); i++) {
			rg_member.getChildAt(i).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					fromApp = false;
					switch (v.getId()) {
					case R.id.rb_user:
						ISAPP = true;
						Albumls = null;
						tv_no_data.setVisibility(View.VISIBLE);
						tv_filter_content.setVisibility(View.GONE);
						if(null!=userTypeAdapter){
							userTypeAdapter.clearDatas();
							userTypeAdapter.notifyDataSetChanged();
						}
						String uName = sp.getString("userName", null);
						if(TextUtils.isEmpty(uName)) {
							tv_no_data.setText("?????????????????????~");
							showUserDialog();
						}else {
							tv_no_data.setText("??????????????????");
							//???????????? TODO
							showLogoutDialog();
						}
						break;
					case R.id.rb_user_alert:
						ISAPP = false;
						USER_TYPE = Constant.TYPE_ZJ;
						tv_no_data.setText("????????????????????????????????????");
						Albumls = dao.queryAllAppsByType(0);//??????
						break;
					case R.id.rb_user_app:
						//app
						ISAPP = true;
						fromApp = true;
						tv_no_data.setText("??????????????????????????????????????????????????????");
						setOftenApp();
						return;
					case R.id.rb_user_collect:
						ISAPP = false;
						USER_TYPE = Constant.TYPE_SC;
						tv_no_data.setText("????????????????????????????????????");
						Albumls = dao.queryAllAppsByType(1);//??????
						break;
					case R.id.rb_user_history:
						ISAPP = false;
						USER_TYPE = Constant.TYPE_LS;
						tv_no_data.setText("??????????????????????????????????????????");
						Albumls = dao.queryAllAppsByType(2);//??????
						break;
					}
					if(!ISAPP){
						if(null!=Albumls && Albumls.size()>0){
							tv_filter_content.setText("??? "+Albumls.size()+" ?????????");
							tv_filter_content.setVisibility(View.VISIBLE);
							tv_no_data.setVisibility(View.GONE);
							userTypeAdapter = new UserTypeAdapter(UserActivity.this, Albumls, imageLoader,ISAPP);
							if(gv_user_type_details_grid.getVisibility() != View.VISIBLE) {
								gv_user_type_details_grid.setVisibility(View.VISIBLE);
							}
							gv_user_type_details_grid.setAdapter(userTypeAdapter);
						}else{
							tv_no_data.setVisibility(View.VISIBLE);
							tv_filter_content.setText("??? 0 ?????????");
							tv_filter_content.setVisibility(View.VISIBLE);
							if(null!=userTypeAdapter){
								userTypeAdapter.clearDatas();
								userTypeAdapter.notifyDataSetChanged();
							}
						}
					}
				}
			});
		}
		
		

		gv_user_type_details_grid.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> pratenView, View v,
					int position, long arg3) {
				mPosition = position;
				//dao.deleteByWhere(Albumls.get(position).getAlbumId(), albumType, typeId)
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		gv_user_type_details_grid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if(!ISAPP){
					mPosition = position;
					showMenu();
				}
				return true;
			}
		});
		gv_user_type_details_grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> pratenView, View v, int position,
					long arg3) {
				if(ISAPP){
					String packname = ((TextView)v.findViewById(R.id.packflag)).getText().toString();
					String appname = ((TextView)v.findViewById(R.id.app_title)).getText().toString();
					Intent intent = getPackageManager().getLaunchIntentForPackage(packname);
					startActivity(intent);
					Logger.d(TAG, "appname="+appname);
					Map<String, String> m_value = new HashMap<String, String>();
					m_value.put("UserAppName", appname);
					m_value.put("UserPackName", packname);
					MobclickAgent.onEvent(UserActivity.this, "USER_APP_NAME", m_value);
				}else{
					Intent intent = new Intent(UserActivity.this,
							VodDetailsActivity.class);
					//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("vodtype", Albumls.get(position).getAlbumType());
					intent.putExtra("vodstate", Albumls.get(position).getAlbumState());
					intent.putExtra("nextlink", Albumls.get(position).getNextLink());
					startActivity(intent);
				}
			}
		});
	
	}
	
	/**
	 * ?????????menu
	 */
	public void onCreateMenu() {
		View menuView = View.inflate(this, R.layout.mv_controler_menu, null);
		menulist = (ListView) menuView.findViewById(R.id.media_controler_menu);
		menupopupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		menupopupWindow.setOutsideTouchable(true);// ??????????????????popu??????
		menupopupWindow.setTouchable(true);
		menupopupWindow.setFocusable(true);
		menulist.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
					return false;
				}
				switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					menupopupWindow.dismiss();
					break;
				}
				return false;
			}
		});

		menulist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				Album album = null;
				if(mPosition!=-1){
					album = Albumls.get(mPosition);
				}
				switch (position) {
				//??????
				case 0:
					if(null!=album){
						dao.deleteByWhere(album.getAlbumId(), album.getAlbumType(), album.getTypeId());
						Albumls  = (ArrayList<Album>) dao.queryAllAppsByType(album.getTypeId());
						userTypeAdapter.remove(mPosition);
						userTypeAdapter.notifyDataSetChanged();
						tv_filter_content.setText("??? "+userTypeAdapter.getCount()+" ?????????");
						//user_type_details_sum.setText("???"+userTypeAdapter.vodDatas.size()+"???");
					}else{
						Utils.showToast(UserActivity.this, "?????????????????????????????????", R.drawable.toast_smile);
					}
					mPosition = -1;
					hideMenu();
					break;
				//????????????
				case 1:
					Albumls  = (ArrayList<Album>) dao.queryAllAppsByType(USER_TYPE);
					if(null!=Albumls&&Albumls.size()>0){
						userTypeAdapter.clearDatas();
						userTypeAdapter.notifyDataSetChanged();
						tv_filter_content.setText("??? 0 ?????????");
						//user_type_details_sum.setText("???0???");
					}else{
						Utils.showToast(UserActivity.this, "?????????????????????????????????", R.drawable.toast_shut);
					}
					dao.deleteAllByWhere(USER_TYPE);
					mPosition = -1;
					hideMenu();
					break;
				}
			}

		});
	}
	// ??????menu
	private void showMenu() {
		if(null!=menupopupWindow){
			mAdapter = new MyAdapter(this, Utils.getUserData(0));
			menulist.setAdapter(mAdapter);
			menupopupWindow.setAnimationStyle(R.style.AnimationMenu);
			menupopupWindow.showAtLocation(user_type_details, Gravity.TOP | Gravity.RIGHT, 0, 0);
			menupopupWindow.update(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_350),mHeight);
		}
	}

	// ??????menu
	private void hideMenu() {
		if (menupopupWindow.isShowing()) {
			menupopupWindow.dismiss();
		}
	}
	
	private List<AppInfo> queryAllOftenApps(){
		templovLst.clear();//???????????????????????????????????????????????????????????????
		//??????????????????????????????????????????app
		List<String> lst = dbtools.queryAll();
		//?????????????????????????????????app
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> packLst = getPackageManager().queryIntentActivities(intent, 0);
		ResolveInfo mInfo = null;
		for (ResolveInfo info:packLst) {//????????????????????????????????????app?????????
			if("com.shenma.tvlauncher".equals(info.activityInfo.packageName)){
				mInfo = info;
				break;
			}
		}
		packLst.remove(mInfo);//????????????launcher
		//??????????????????app????????????
		for (String pkgname : lst) {
			for (ResolveInfo info : packLst) {
				if(pkgname.equals(info.activityInfo.packageName)){
					AppInfo ai = new AppInfo();
					ai.setAppicon(info.loadIcon(getPackageManager()));
					ai.setAppname(info.loadLabel(getPackageManager()).toString());
					ai.setApppack(info.activityInfo.packageName);
					ai.setLove(true);
					templovLst.add(ai);
					//mInfo = info;
				}
			}
			//packLst.remove(mInfo);//???????????????app
		}
		return templovLst;
	}
	
	/**
	 * ???????????????????????????dialog
	 */
	private void showUserDialog(){
		WiFiDialog.Builder builder = new WiFiDialog.Builder(context);
		View mView = View.inflate(context, R.layout.user_form, null);
		final EditText user_name_et = (EditText) mView.findViewById(R.id.user_name_et);
		final EditText user_pass_et = (EditText) mView.findViewById(R.id.user_pass_et);
		builder.setContentView(mView);
		builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//????????????
				requestLogin(user_name_et, user_pass_et);
			}
		});
		builder.setNeutralButton("??????", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//????????????
				requestServer(user_name_et, user_pass_et, Constant.USERREG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						try {
							Utils.loadingClose_Tv();
							JSONObject jo = new JSONObject(response);
							int code = jo.optInt("code");
							String msg = jo.optString("msg");
							if(code == 200) {
								//???????????? ????????????
								requestLogin(user_name_et, user_pass_et);
							}else {
								Utils.showToast(context, msg, R.drawable.toast_err);
							}
							Logger.d("zhouchuan", "Server feedback this code "+ code + " msg " + msg);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					
				});
			}
		});
		mDialog = builder.create();
		mDialog.show();
	}
	
	/**
	 * ??????????????????
	 */
	private void showLogoutDialog() {
		WiFiDialog.Builder builder = new WiFiDialog.Builder(context);
		View mView = View.inflate(context, R.layout.logout_dialog, null);
		builder.setContentView(mView);
		builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//????????????
				sp.edit().putString("userName", null).putString("passWord", null).commit();
				tv_no_data.setText("?????????????????????~");
				tv_user_name.setText(R.string.no_login);
				dialog.dismiss();
			}
		});
		builder.setNeutralButton("??????", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		mDialog = builder.create();
		mDialog.show();
	}
	
	/**
	 * ???????????????
	 * @param uNameET	??????????????????
	 * @param uPassET	???????????????
	 * @param requestUrl??????????????????
	 * @param sucListener	????????????
	 * @return ??????md5??????????????????
	 */
	private void requestServer(EditText uNameET, EditText uPassET,String requestUrl, Response.Listener<String> sucListener) {
		//????????????
		final String userName = uNameET.getText().toString().trim();
		String passWord = uPassET.getText().toString().trim();
		//????????????
		if(TextUtils.isEmpty(userName)) {
			Utils.showToast(context, "?????????????????????", R.drawable.toast_err);
			return;
		} if (TextUtils.isEmpty(passWord)) {
			Utils.showToast(context, "?????????????????????", R.drawable.toast_err);
			return;
		}
		md5Pass = Md5Encoder.encode(Md5Encoder.encode(passWord));
		if(requestUrl.equals(Constant.USERLOGIN)) {
			Utils.loadingShow_tv(context, R.string.is_loading);
		}else {
			Utils.loadingShow_tv(context, R.string.is_registing);
		}
		
		StringRequest sr = new StringRequest(Method.POST, requestUrl, sucListener, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Utils.showToast(context, "??????,????????????????????????", R.drawable.toast_err);
			}
			
		}){
			@Override
	        public Map<String, String> getHeaders() throws AuthFailureError {
				HashMap<String, String> headers = new HashMap<String, String>();
				String base64 = new String(android.util.Base64.encode("admin:1234".getBytes(), android.util.Base64.DEFAULT));
				headers.put("Authorization", "Basic " + base64);
	        	return headers;
	        }
			
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
			 	Map<String,String> params = new HashMap<String, String>();
	            params.put("loginname",userName);
	            params.put("password",md5Pass);
	            Logger.d("zhouchuan", "userName="+userName+" md5Pass="+md5Pass);
	            return params;
			}
		};
		mQueue.add(sr);
	}
	
	/**
	 * ????????????
	 * @param uNameET ??????????????????
	 * @param uPassET ???????????????
	 */
	private void requestLogin(EditText uNameET, EditText uPassET) {
		final String userName = uNameET.getText().toString().trim();
		requestServer(uNameET, uPassET, Constant.USERLOGIN, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {
					Utils.loadingClose_Tv();
					Logger.d("zhouchuan", response);
					JSONObject jo = new JSONObject(response);
					int code = jo.optInt("code");
					String msg = jo.optString("msg");
					if(code == 200) {
						//???????????? ???????????? ????????????
						tv_user_name.setText(userName);
						tv_no_data.setText("??????????????????");
						String ck = jo.getJSONObject("data").optString("ckinfo");
						Logger.d("zhouchuan", "ckinfo is "+ck);
						//?????????????????????????????????????????????ck???
						sp.edit().putString("userName", userName).putString("passWord", md5Pass).putString("ckinfo", ck).commit();
						if(mDialog != null && mDialog.isShowing()) {
							mDialog.dismiss();
						}
					}else {
						Utils.showToast(context, msg, R.drawable.toast_err);
					}
					Logger.d("zhouchuan", "Server feedback this code "+ code + " msg " + msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 */
	class MyAdapter extends BaseAdapter {
		private Context context;
		ArrayList<String> mylist;

		public MyAdapter(Context context, ArrayList<String> mylist) {
			this.context = context;
			this.mylist = mylist;
		}

		@Override
		public int getCount() {
			return mylist.size();
		}

		@Override
		public Object getItem(int position) {
			return mylist.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = LayoutInflater.from(context).inflate(R.layout.mv_controler_menu_item, null);
			TextView tv = (TextView) v.findViewById(R.id.tv_menu_item);
			tv.setText(mylist.get(position));
			return v;
		}

	}

	private final String TAG = "UserActivity";
	private List<AppInfo> templovLst = new ArrayList<AppInfo>();
	private Boolean ISAPP = true,fromApp = false;
	private FinalHttp fh;
	private RequestQueue mQueue;
	private TextView tv_user_name;
	private Dialog mDialog;
	private String md5Pass;
}
