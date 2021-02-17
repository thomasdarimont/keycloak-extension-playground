<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Authenticate with your Face
    <#elseif section = "header">
        Please authenticate with your Face
    <#elseif section = "form">

        <div style="position: relative" class="margin">
            <video onloadedmetadata="onPlay(this)" id="inputVideo" autoplay muted playsinline>
                Loading Face Auth...
            </video>
<#--            <canvas id="overlay"></canvas>-->
        </div>

        <script src="http://localhost:3000/face-api.js"></script>

        <script defer>

            const SSD_MOBILENETV1 = 'ssd_mobilenetv1'
            const TINY_FACE_DETECTOR = 'tiny_face_detector'

            let selectedFaceDetector = SSD_MOBILENETV1

            // ssd_mobilenetv1 options
            let minConfidence = 0.5

            // tiny_face_detector options
            let inputSize = 128
            let scoreThreshold = 0.5

            function getFaceDetectorOptions() {
                return selectedFaceDetector === SSD_MOBILENETV1
                    ? new faceapi.SsdMobilenetv1Options({ minConfidence })
                    : new faceapi.TinyFaceDetectorOptions({ inputSize, scoreThreshold })
            }

            function getCurrentFaceDetectionNet() {
                if (selectedFaceDetector === SSD_MOBILENETV1) {
                    return faceapi.nets.ssdMobilenetv1
                }
                if (selectedFaceDetector === TINY_FACE_DETECTOR) {
                    return faceapi.nets.tinyFaceDetector
                }
            }

            function isFaceDetectionModelLoaded() {
                return !!getCurrentFaceDetectionNet().params
            }

            const minFaceScoreThreshold = 0.7;

            const $inputVideo = document.getElementById("inputVideo");
            const $overlay = document.getElementById("overlay");

            let analyzingFace = false;

            async function onPlay() {
                if ($inputVideo.paused || $inputVideo.ended || !isFaceDetectionModelLoaded()) {
                    return setTimeout(() => onPlay(), 1000)
                }

                if (analyzingFace) {
                    setTimeout(() => onPlay(), 1000)
                    return;
                }

                const options = getFaceDetectorOptions();
                const singleFaceResult = await faceapi.detectSingleFace($inputVideo, options)

                if (!singleFaceResult) {
                    setTimeout(() => onPlay(), 1000);
                    return;
                }

                // const dims = faceapi.matchDimensions($overlay, $inputVideo, true)
                // faceapi.draw.drawDetections($overlay, faceapi.resizeResults(singleFaceResult, dims))

                if (singleFaceResult.score < minFaceScoreThreshold) {
                    setTimeout(() => onPlay(), 1000);
                    return;
                }

                if (window.faceResult) {
                    setTimeout(() => onPlay(), 1000);
                    return;
                }

                console.log("found face");
                analyzingFace = true;

                // for debugging
                window.faceResult = singleFaceResult;

                console.log("use face...");

                let faceCanvases = await faceapi.extractFaces($inputVideo, [singleFaceResult.box]);
                if (faceCanvases.length === 0) {
                    setTimeout(() => onPlay(), 1000);
                    return;
                }

                let faceCanvas = faceCanvases[0];
                let faceBlob = await toBlob(faceCanvas);

                let dataUrl = await readAsDataURL(faceBlob);
                let faceAuthData = { faceImage: dataUrl };

                let loginActionUrl = document.getElementById("kc-u2f-login-form").action;

                // let formData = new FormData();
                // formData.append('faceImage', dataUrl);

                let faceAuthResponse = await fetch(loginActionUrl, {
                    method: "post",
                    redirect: 'manual',
                    body: JSON.stringify(faceAuthData),
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                // if (faceAuthResponse.status === 200) {
                //     console.log("proceed...");
                //     window.location.reload();
                //     return;
                // }

                if (faceAuthResponse.status === 302 || faceAuthResponse.type ==="opaqueredirect") {
                    window.location.href = faceAuthResponse.url;
                    return;
                }

                // let faceAuthResponseJson = await faceAuthResponse.json();
                // console.log(faceAuthResponseJson);
                //
                // if (faceAuthResponseJson.username === "unknown") {

                    window.faceResult = null;
                    analyzingFace = false;

                    console.log("try again");
                    setTimeout(() => onPlay(), 1000);
                    // return;
                // }

                // console.log("done");

                // setTimeout(() => onPlay());
            }

            async function toBlob(canvas) {
                return new Promise((resolve, reject) => {
                    canvas.toBlob(blob => resolve(blob));
                });
            }

            async function readAsDataURL(blob) {
                return new Promise((resolve, reject) => {
                    let fr = new FileReader();
                    fr.onload = () => {
                        return resolve(fr.result);
                    };
                    fr.readAsDataURL(blob);
                });
            }

            async function startFaceDetection() {
                // load face detection model
                // await changeFaceDetector(TINY_FACE_DETECTOR)
                // changeInputSize(128)

                if (!isFaceDetectionModelLoaded()) {
                    await getCurrentFaceDetectionNet().load('http://localhost:3000/')
                }

                // try to access users webcam and stream the images
                // to the video element
                const stream = await navigator.mediaDevices.getUserMedia({ video: {} })
                $inputVideo.srcObject = stream
            }

            startFaceDetection();
        </script>

        <form id="kc-u2f-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
<#--                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"-->
<#--                           type="submit" value="${msg("doSubmit")}"/>-->
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" name="cancel" value="${msg("doCancel")}"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>