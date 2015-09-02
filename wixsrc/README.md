Windows Installer XML
=====================

This directory contains the [Windows Installer XML](http://wixtoolset.org/)
sources to build a full ``binnavi600-win-x86.msi``. While the code was in use
at zynamics, it won't work out of the box for the open source project and
should be considered a work in progress.

Other things of note:
* The source files were written for WiX 3.5.2519, which is old-ish
* This directory also contains a DoxyFile that is used to generate the
  official documentation.
* A ``build.scons`` file that invokes Doxygen and the Wix compiler is
  included for reference.
