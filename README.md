[![](https://github.com/tinevez/TrackMate-Kymograph/actions/workflows/build-main.yml/badge.svg)](https://github.com/tinevez/TrackMate-Kymograph/actions/workflows/build-main.yml)

# Generating and analyzing kymographs with TrackMate

![A_MovingKymographExample](https://github.com/user-attachments/assets/a2bc3bef-ecba-4b07-9185-4a0eb1fd8c22)


Kymographs are a very useful technique in bioimage analysis, that allow for visually following particles even in data where they are very faint in a noisy background. A moving object drown in noise and background signal could be become barely visible in the source image. In a kymograph its movement often becomes salient, and easier to analyze.

Kymographs rely on a dimensionality reduction that generate a 2D image from a 2D+T or a 3D+T source. They are created by measuring the intensity profile over a line between two fixed points in one time point of the input image. The intensity profile is turned into a 1D line image, and all the 1D line images across all time-points are stacked in the target kymograph. In the 2D image of a kymograph, the time runs along Y from top to bottom, and the space runs along the X dimension and reports the intensity along the line.

The TrackMate-Kymograph extension addresses one issue with the above approach. It is aimed at generating a kymograph between moving points, that we call landmarks in the following. The landmarks that define the kymograph line are simply taken from two tracks in a TrackMate session. The input data can be 2D or 3D. The line would follow a biological structure of interest over time, possibly moving, and we generate a kymograph along this line. To our knowledge, all the other tools that can generate kymographs only work when the line is fixed. The following short documentation shows how to install the plugin and how to use it.

This TrackMate extension is documented on the ImageJ Wiki: https://imagej.net/plugins/trackmate/extensions/trackmate-kymograph 
