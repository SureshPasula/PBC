git add -A *
git pull $REPOSITORY_URL master
git commit -m "BAR Artifact is deployed in to git repository"
git push --repo=$REPOSITORY_URL -f
