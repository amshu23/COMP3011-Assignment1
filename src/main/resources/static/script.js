let recorder;
let audioChunks = [];

const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusMessage = document.getElementById("statusMessage");
const resultText = document.getElementById("resultText");

startButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);

async function startRecording() {

    try {
        const stream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        audioChunks = [];

        recorder = new MediaRecorder(stream);

        recorder.addEventListener("dataavailable", event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

        recorder.addEventListener("stop", async () => {

            stream.getTracks().forEach(track => track.stop());

            const audioBlob = new Blob(audioChunks, {
                type: recorder.mimeType
            });

            await sendAudio(audioBlob);
        });

        recorder.start();

        startButton.disabled = true;
        stopButton.disabled = false;

        statusMessage.textContent = "Recording...";
        resultText.textContent = "";

    } catch (error) {

        console.error(error);

        statusMessage.textContent =
            "Could not access your microphone.";
    }
}

function stopRecording() {

    if (recorder && recorder.state === "recording") {

        recorder.stop();

        startButton.disabled = true;
        stopButton.disabled = true;

        statusMessage.textContent = "Transcribing...";
    }
}

async function sendAudio(audioBlob) {

    const formData = new FormData();

    formData.append(
        "file",
        audioBlob,
        "recording.webm"
    );

    try {

        const response = await fetch("/api/transcribe", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            throw new Error(
                "Transcription request failed."
            );
        }

        const transcription = await response.text();

        resultText.textContent = transcription;

        statusMessage.textContent = "Transcription complete.";

    } catch (error) {

        console.error(error);

        statusMessage.textContent =
            "Something went wrong while transcribing.";

        resultText.textContent =
            "Unable to transcribe the recording.";

    } finally {

        startButton.disabled = false;
        stopButton.disabled = true;
    }
}