git config --global user.email suresh.pasula@prolifics.com
git config --global user.name spasula
git config --bool core.bare true
git add -A ./PBC/scribblesecond.bar 
git commit -m "BAR Artifact is deployed in to git repository"
git pull https://SureshPasula:prolifics1@github.com/SureshPasula/PBC.git
git push origin HEAD:master 



