# Media file uploader
<h3>Basic Usage</h3>
<p>Upload single and multiple media files.</p>
<p>Be able to easily implement.</p>
<p>You can must set base upload url.</p>

<h4>Code (Samples)</h4>
<h5><ul>Add single file path and type</ul></h5>
<pre>
		ArrayList&lt;Media&gt; mediaList = new ArrayList&lt;&gt;();
		Uri selectedMediaUri = data.getData();
		Media media = new Media();
		media.setUri(getPath(this, selectedMediaUri));
			 if (selectedMediaUri.toString().contains("image")) {
					//handle image
					media.setType("image");
			} else if (selectedMediaUri.toString().contains("video")) {
					//handle video
					media.setType("video");
			}
		mediaList.add(media);
</pre>

<h5><ul>Add Multiple file path and type</ul></h5>

<pre>
		ArrayList&lt;Media&gt; mediaList = new ArrayList&lt;&gt;();
		ClipData mClipData = data.getClipData();
  			for (int i = 0; i &lt; mClipData.getItemCount(); i++) {
 					ClipData.Item item = mClipData.getItemAt(i);
					Uri selectedMediaUri = item.getUri();
					Media media = new Media();
					media.setUri(getPath(this, selectedMediaUri));
						if (selectedMediaUri.toString().contains("image")) {
								//handle image
								media.setType("image");
						} else if (selectedMediaUri.toString().contains("video")) {
								//handle video
								media.setType("video");
						}
					mediaList.add(media);
			}
</pre>

<h5><ul>Set media list into Upload data object</ul></h5>

<pre>
		UploadData uploadData = new UploadData();
		// upload id
		int uploadId = (int) System.currentTimeMillis();
		// set media list into object
		uploadData.setUploadId(uploadId);
		uploadData.setMediaList(mediaList);
</pre>

<h5><ul>Request Upload Method</ul></h5>

<pre>
	private void requestUpload(Context context, String paramsName, String pathName, final UploadData uploadData, final int uploadId) {
			uploadData
					// set base upload url
					.setUrl(BASE_URL)
					// register status receiver
					.setStatusReceiver(registerStatusReceiver())
					// set header
					.setHeader("Content-Type", "multipart/form-data")
					.setHeader("Authorization", "Bearer" + " token")
					// set params
					.setParams("id", paramsName);

		// service call
		new FileUploadService()
					// set max retries
					.setMaxRetries(0)
					// set syc
					.setSyc(this, false)
					// set notification config
					.setNotificationConfig(getNotificationConfig(this, R.string.app_name))
					// create service call
					.serviceCall(context, paramsName, pathName, uploadData, uploadId);
	}
</pre>

<h5><ul>Notification custom configuration</ul></h5>

<pre>
public UploadNotificationConfig getNotificationConfig(Context context, @StringRes int title) {
				// PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Main2Activity.class), 0);
				UploadNotificationConfig config = new UploadNotificationConfig();
				config.getProgress().title = "Progress";
				config.getProgress().message = context.getString(com.tartlabs.mediafileupload.R.string.in_progress);
				config.getProgress().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
				config.getProgress().iconColorResourceID = Color.BLUE;
				//config.getProgress().clickIntent = pendingIntent;
				//config.getProgress().clearOnAction = true;

				config.getCompleted().title = "Completed";
				config.getCompleted().message = context.getString(com.tartlabs.mediafileupload.R.string.upload_success);
				config.getCompleted().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
				config.getCompleted().iconColorResourceID = Color.GREEN;
				// config.getCompleted().clearOnAction = true;

				config.getError().title = "Error";
				config.getError().message = context.getString(com.tartlabs.mediafileupload.R.string.upload_error);
				config.getError().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
				config.getError().iconColorResourceID = Color.RED;
				config.getError().clearOnAction = false;
				//config.getError().clickIntent = PendingIntent.getActivity(this, 0, new Intent(this, Main2Activity.class), 0);

			config.getCancelled().title = "Cancelled";
			config.getCancelled().message = context.getString(com.tartlabs.mediafileupload.R.string.upload_cancelled);
			config.getCancelled().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
			config.getCancelled().iconColorResourceID = Color.YELLOW;
			// config.getCancelled().clearOnAction = true;
		return config;
}
</pre>

<h5><ul>Register call back receiver</ul></h5>

<pre>
	private BroadcastReceiver registerStatusReceiver() {
			IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
			BroadcastReceiver receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
						//extract our message from intent
						int uploadId = intent.getIntExtra("uploadId", 0);
						String status = intent.getStringExtra("status");
						//show our message value
						showToast(" uploadId " + uploadId);
						showToast(" status " + status);
				}
			};
			//registering our receiver
			this.registerReceiver(receiver, intentFilter);
		return receiver;
	}
</pre>

<h3>Features</h3>

<ul>
	<li>Able to upload multiple media files.</li>
	<li>Upload files to a server with HTTP multipart/form-data</li>
	<li>You can Set maximum retries - value datatype integer (optional)</li>
	<li>You can Set background syc - value datatype boolean (optional)</li>
	<li>You can Set notification configuration (optional)</li>
	<li>You can Set headers and parameters (optional)</li>
	<li>You can register call back receiver (optional)</li>
	<li>If you not give any optional value its take default values</li>
</ul>

<h3>Call back receiver</h3>
<p> If you register the call back receiver, its return Upload status and upload id</p>
<p>Upload status parsing key <code>status</code></p>
<p>Upload id parsing key <code>uploadId</code></p>
<p>Two type of status return <code>1. success</code>  <code>2. failure</code></p>
