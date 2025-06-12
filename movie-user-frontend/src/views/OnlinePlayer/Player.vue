<template>
  <div class="player-page" @click="toggleMute" title="Click to toggle mute">
    <div class="video-container">
      <video 
        ref="videoPlayer" 
        class="video-element" 
        autoplay 
        muted
        playsinline
        disablepictureinpicture
        @contextmenu.prevent
      ></video>
    </div>

    <div class="overlay">
      <div v-if="isLoading" class="status-box">
        <el-icon class="is-loading text-4xl"><Loading /></el-icon>
        <p class="mt-2">Entering the theater, please wait...</p>
      </div>
      <div v-else-if="error" class="status-box error-box">
        <el-icon class="text-4xl mb-2"><CircleCloseFilled /></el-icon>
        <p class="font-bold">Playback Failed</p>
        <p class="text-sm mt-1">{{ error }}</p>
        <el-button type="primary" round @click.stop="goBack" class="mt-4">Go Back</el-button>
      </div>
      
      <div v-if="!isLoading && !error" class="volume-indicator">
          <transition name="fade">
              <el-icon v-if="showVolumeIcon" class="text-5xl">
                  <Mute v-if="isMuted" />
                  <Microphone v-else />
              </el-icon>
          </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Hls from 'hls.js';
import { Loading, CircleCloseFilled, Mute, Microphone } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const videoPlayer = ref(null);
const hlsInstance = ref(null);
const isLoading = ref(true);
const error = ref('');
const isMuted = ref(true);
const showVolumeIcon = ref(false);
let volumeIconTimer = null;

const goBack = () => router.back();

const toggleMute = () => {
    if (videoPlayer.value && !isLoading.value && !error.value) {
        videoPlayer.value.muted = !videoPlayer.value.muted;
        isMuted.value = videoPlayer.value.muted;
        showVolumeIcon.value = true;
        clearTimeout(volumeIconTimer);
        volumeIconTimer = setTimeout(() => { showVolumeIcon.value = false; }, 2000);
    }
};

onMounted(() => {
  const { streamUrl: encodedStreamUrl, syncTime } = route.query;

  if (!encodedStreamUrl || syncTime === undefined) {
    error.value = 'Playback information is missing or invalid. Please try again from the order page.';
    isLoading.value = false;
    return;
  }

  try {
    const streamUrl = decodeURIComponent(decodeURIComponent(encodedStreamUrl));
    const syncTimeNum = parseFloat(syncTime);
    const videoElement = videoPlayer.value;

    if (!videoElement) {
        throw new Error('Player element not found in DOM.');
    }

    if (Hls.isSupported()) {
      const hls = new Hls({
        startPosition: syncTimeNum,
      });
      hlsInstance.value = hls;
      
      hls.loadSource(streamUrl);
      hls.attachMedia(videoElement);

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        isLoading.value = false;
        videoElement.play().catch(playError => {
            console.warn('Autoplay was prevented by browser:', playError);
            error.value = 'Autoplay was blocked by the browser. Click the screen to start.';
            const startOnClick = () => {
                videoElement.play();
                document.body.removeEventListener('click', startOnClick);
                error.value = '';
            };
            document.body.addEventListener('click', startOnClick, { once: true });
        });
      });

      hls.on(Hls.Events.ERROR, (event, data) => {
        if (data.fatal) {
            console.error('HLS Fatal Error:', data);
            isLoading.value = false;
            if (data.type === Hls.ErrorTypes.NETWORK_ERROR && data.response?.code === 403) {
                error.value = 'Your viewing ticket is invalid or has expired. Please re-enter.';
            } else if (data.type === Hls.ErrorTypes.NETWORK_ERROR && data.response?.code === 500) {
                error.value = 'Internal server error at the cinema. Please try again later.';
            } else {
                error.value = 'Network connection interrupted, unable to load the movie.';
            }
            hls.destroy();
        }
      });
    } else {
        throw new Error('Your browser does not support HLS playback.');
    }
  } catch(e) {
      console.error('Playback initialization failed:', e);
      isLoading.value = false;
      error.value = e.message;
  }
});

onBeforeUnmount(() => {
  if (hlsInstance.value) {
    hlsInstance.value.destroy();
  }
  clearTimeout(volumeIconTimer);
});
</script>

<style scoped>
.player-page {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background-color: #000;
  display: flex; justify-content: center; align-items: center;
  z-index: 2000;
  cursor: pointer;
}
.video-container, .video-element {
  width: 100%; height: 100%;
}
.video-element {
  object-fit: contain;
  pointer-events: none;
}
.video-element::-webkit-media-controls { display: none !important; }
video::-webkit-media-controls-enclosure { display: none !important; }

.overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  display: flex; justify-content: center; align-items: center;
  color: white;
  pointer-events: none;
}
.status-box {
  background-color: rgba(0, 0, 0, 0.7);
  padding: 2rem; border-radius: 1rem; text-align: center;
  pointer-events: auto;
}
.error-box {
  border: 1px solid #ef4444;
}
.volume-indicator {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>