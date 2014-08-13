git add -A *
git pull $REPOSITORY_URL master
git commit -m "BAR Artifact is deployed in to git repository"
git push --repo=https://$REPOSITORY_USERNAME:$REPOSITORY_PASSWORD@github.com/PBC.git -f
