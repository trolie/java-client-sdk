# Maintenance and Contacts
For most inquiries, create GitHub issues or contact `trolie-general@lists.lfenergy.org`.  

## Release, Maven Central deployment
CI build currently only executes Maven install.  Deployment to Maven Central is done automatically on release, via
the `release.yml` workflow.  This expects that Git tags for releases must be formatted per standard recommendation- for
example, when releasing 1.0.0, the tag should be `v1.0.0`.  

After releasing, the `mvn versions:set -DnewVersion=<newVersion>` command must be run and commit manually on the main branch, 
with `<newVersion>` set to the new planned development version.  