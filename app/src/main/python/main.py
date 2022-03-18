import pafy
import requests
from youtubesearchpython import VideosSearch

default_url = f"http://www.youtube.com/watch?v="

def get_results(title):
   ret = []
   try:
      video_res = VideosSearch(title).result()["result"]
   except:
      return ret
   for video in video_res:
      id = video["id"]
      vid_url = default_url + id
      vid_title = video["title"]
      vid_channel = video["channel"]["name"]
      thumb = video["thumbnails"][0]["url"]
      ret.append([vid_url,vid_title,vid_channel,thumb])
   return ret


def get_vid_data(url):
    try:
       video_data = pafy.new(url)
    except:
       return []
    audio = video_data.getbestaudio()
    stream_url = audio.url
    duration = video_data.length
    return url,stream_url,duration