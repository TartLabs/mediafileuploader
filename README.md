# Media file uploader
<h3>Basic Usage</h3>
  <p> Upload single and multiple media files.</p>
  <p> Be able to easily implement.</p>
  <p> You can must set base upload url.</p>
<h3>Features</h3>
  <ul>
    <li>Able to upload multiple media files.</li>
    <li>You can Set maximum retries - value datatype integer (optional)</li>
    <li>You can Set background syc - value datatype boolean (optional)</li>
    <li>You can Set notification configuration (optional)</li>
    <li>You can Set headers and parameters (optional)</li>
    <li>You can register call back receiver (optional)</li>
    <li>If you not give any optional value its take default values</li>
  </ul>
<h3>Call back receiver</h3>
  <p> If you register the call back receiver, its return Upload status and upload id</p>
  <p> Upload status parsing key <code>status</code></p>
  <p> Upload id parsing key <code>uploadId</code>
  <p> Two type of status return <code>1. success</code>  <code>2. failure</code></p>
