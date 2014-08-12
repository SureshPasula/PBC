git config --global user.email suresh.pasula@prolifics.com
git config --global user.name SureshPasula
git add -A ./PBC/scribblesecond.bar 
git commit -m "BAR Artifact is deployed in to git repository"
git pull git@github.com:SureshPasula/PBC.git master
git push origin HEAD.master 