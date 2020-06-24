# CUJ Recording Project

**This is not an officially supported Google product.**

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

    Copyright 2020 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


## Usage

### executeCuj

To get data on the latency of a CUJ running on a connected device, run 

```bash
./executeCuj <input_file>
```

where input file is formatted as follows (all on seperate lines):

```
<preparatory_actions> 
<measured_actions> 
<num_iterations> 
<r?>
```

preparatory_actions consists of actions to be performed prior to the CUJ 
in question and measured actions consists of the CUJ in question plus a final 
"termination" action, which can only be performed once the last action in the 
CUJ has fully terminated. num_iterations denotes the number of iterations 
you'd like the test to run through your CUJ. Optionally, end the input with 
the line "r" to signify that you'd like the run of median length to be outputted 
by the program. (Note that if you'd like to record, you need to have **ffmpeg 
installed on your machine** (Ubuntu instructions here: https://tecadmin.net/install-ffmpeg-on-linux/)
and the entire test, including each iteration, much have length <= 3 minutes). 
The resulting video will be called "median_clip.mp4"

the action list lines must be formatted as follows:

```bash
['<action1>', '<action2>', ... , '<actionN>']
```
Each action is either to launch an app, click on a particular View or to enter 
text in a particular textbox. Actions are represented in one of the following forms:
     

1. 'start;package_name' to launch the specified package.
2. 'edit;text_displayed;text_entered' to enter text_entered into a textbox, where 
    text_displayed is the text currently visible in the textbox.
3. 'click;text_displayed' to click on a view on the screen which has text_displayed 
    as a substring of the text currently visible on it.
4. 'clickImage;text_description' to click a view without text which has text_description 
    as a substring of its description. (One might be able to guess this, but if one is 
    wrong, the script will return an error message with the descriptions of images to 
    choose from).

     
Note that all text fields are case and space sensitive. Additionally, if an action 
of form 2, 3, or 4 is followed by “;strict” the search for a corresponding view 
will enforce that an exact match is found for text_displayed (in cases 2 or 3) or
text_description (in case 4).


#### Example:
A text file you would feed to ``executeCuj`` might look like (also available in CUJExecutor/directions_to_googleplex.txt):
```
['start;com.google.android.apps.maps', 'edit;Search here;Googleplex', 'click;Amphitheatre']
['click;Directions;strict', 'click;Choose starting', 'click;Your location;strict', 'click;Steps & more;strict']
5
r
```
Here, the CUJ in question starts after the user has searched for and selected Googleplex at Amphitheatre Parkway and consists of the actions:

1. click on "Directions"
2. click on "Choose starting location"
3. click on "Your location"

To signal when the last action in the CUJ has terminated, the input also includes the "termination" action

4. click on "Steps & more"  

This signifies that action 3 is over once the Steps & more button is clickable, which occurs only once the directions have fully loaded.

The file additionally specifies that it would like the CUJ to run 5 times and that it would like the script to return a clip of the run that took the median total length

### stitch

to stitch together several videos to compare them side-by-side, use the stitch command as follows:

```bash
./stitch <video1> <video2> ... <videoN>
```

the resulting video will be called "stitched.mp4" 

## Demo
The following video was produced by running the `stitch` script on four videos produced by running the `executeCuj` script on `CUJExecutor/takeout.txt` (where the CUJ in question consists of clicking on "Takeout"):

![](takeout_demo.gif)
