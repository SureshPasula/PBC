git config --global user.email suresh.pasula@prolifics.com
git config --global user.name spasula
git add -A ./PBC/scribblesecond.bar 
git commit -m "BAR Artifact is deployed in to git repository"
git pull https://SureshPasula@github.com/SureshPasula/PBC.git master
git push ssh://git@github.com:SureshPasula:prolifics1/PBC.git 

