package org.bff.javampd;

import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDDatabaseException;
import org.bff.javampd.exception.MPDException;
import org.bff.javampd.integrationdata.*;
import org.bff.javampd.objects.MPDAlbum;
import org.bff.javampd.objects.MPDArtist;
import org.bff.javampd.objects.MPDGenre;
import org.bff.javampd.objects.MPDSong;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MPDDatabaseIT extends BaseTest {
    private static List<MPDArtist> artistList;
    private static List<MPDAlbum> albumList;
    private static List<MPDGenre> genreList;
    private static List<MPDSong> songList;
    private static final String SEARCH_ANY = "2";
    private static final String SEARCH_ARTIST = "Artist";
    private static final String SEARCH_ALBUM = "Album";
    private static final String SEARCH_TITLE = "Title";
    private static final String SEARCH_TITLE_SPACE = "Title 1";
    private static final String FIND_ARTIST = "Artist1";
    private static final String FIND_GENRE = "Rock";
    private static final String FIND_ALBUM = "Album1";
    private static final String FIND_TITLE = "Title1";
    private static final String FIND_ANY = "Artist0";
    private static final String FIND_YEAR = "1990";

    private TestProperties testProperties;

    @Before
    public void setUp() throws MPDException, IOException {
        testProperties = TestProperties.getInstance();

        if (getArtistList() == null) {
            setArtistList(new ArrayList<MPDArtist>(getDatabase().listAllArtists()));
        }

        if (getAlbumList() == null) {
            setAlbumList(new ArrayList<MPDAlbum>(getDatabase().listAllAlbums()));
        }

        if (getGenreList() == null) {
            setGenreList(new ArrayList<MPDGenre>(getDatabase().listAllGenres()));
        }

        if (getSongList() == null) {
            setSongList(new ArrayList<>(Songs.databaseSongs));
        }
    }

    @Test
    public void testListRootDirectory() throws IOException, MPDConnectionException, MPDDatabaseException {
        List<File> testFiles = new ArrayList<>(Files.getRootTestFiles(testProperties.getPath()));
        List<MPDFile> files = new ArrayList<>(getDatabase().listRootDirectory());

        Assert.assertEquals(testFiles.size(), files.size());

        for (File f : testFiles) {
            boolean found = false;
            for (MPDFile mpdF : getDatabase().listRootDirectory()) {
                if (f.getName().equals(mpdF.getName())) {
                    found = true;
                    Assert.assertEquals(f.isDirectory(), mpdF.isDirectory());
                }
            }
            if (!found) {
                System.out.println("didnt find:" + f.getName());
            }
            Assert.assertTrue(found);
        }
    }

    @Test
    public void testListDirectories() throws Exception {
        List<File> testFiles = new ArrayList<>(Files.getRootTestFiles(testProperties.getPath()));

        for (File f : testFiles) {
            for (MPDFile mpdF : getDatabase().listRootDirectory()) {
                if (f.getName().equals(mpdF.getName()) && f.isDirectory()) {
                    compareDirs(f, mpdF);
                }
            }
        }
    }

    @Test
    public void testArtistCount() throws MPDException {
        Assert.assertEquals(Artists.artists.size(), getDatabase().getArtistCount());
    }

    @Test
    public void testAlbumCount() throws MPDException {
        Assert.assertEquals(Albums.albums.size(), getDatabase().getAlbumCount());
    }

    @Test
    public void testSongCount() throws MPDException {
        Assert.assertEquals(Songs.songs.size(), getDatabase().getSongCount());
    }

    @Test
    public void testGenreCount() throws MPDException {
        Assert.assertEquals(Genres.genres.size(), getDatabase().listAllGenres().size());
    }

    @Test
    public void testSongs() throws Exception {

        for (MPDSong song : getSongList()) {
            boolean exists = false;
            for (MPDSong s : Songs.songs) {
                if (song.getFile().equals(s.getFile())) {
                    exists = true;
                    compareSongs(s, song);
                    break;
                }
            }

            if (!exists) {
                Assert.fail("Song does not exist in list.");
            }
        }
    }

    @Test
    public void testAlbumList() {

        for (MPDAlbum a : Albums.albums) {
            boolean exists = false;
            String albumName = a.getName();
            for (MPDAlbum album : getAlbumList()) {
                if (album.getName().equals(albumName)) {
                    exists = true;
                    Assert.assertEquals(album.getName(), a.getName());
                    break;
                }
            }

            if (!exists) {
                Assert.fail("Album " + a + " does not exist in list.");
            }
        }
    }

    @Test
    public void testGetYears() throws MPDException {
        List<String> resultYears = new ArrayList<String>(getDatabase().listAllYears());

        List<String> foundYears = new ArrayList<String>(Years.years);

        Assert.assertEquals(resultYears.size(), foundYears.size());

        for (String year : resultYears) {
            boolean exists = false;

            for (String y : foundYears) {
                if (year.equals(y)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                Assert.fail("Year " + year + " does not exist in list.");
            }
        }
    }

    @Test
    public void testFindArtist() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(FIND_ARTIST + "-")) {
                testResults.add(song);
            }
        }

        MPDArtist artist = new MPDArtist(FIND_ARTIST);
        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findArtist(artist));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindArtistString() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(FIND_ARTIST + "-")) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findArtist(FIND_ARTIST));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindAlbum() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains("-" + FIND_ALBUM + "-")) {
                testResults.add(song);
            }
        }

        MPDAlbum album = new MPDAlbum(FIND_ALBUM);
        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findAlbum(album));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindAlbumString() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(FIND_ALBUM + "-")) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findAlbum(FIND_ALBUM));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindGenre() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains("-" + FIND_GENRE + "-")) {
                testResults.add(song);
            }
        }

        MPDGenre genre = new MPDGenre(FIND_GENRE);
        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findGenre(genre));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindGenreString() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(FIND_GENRE + "-")) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findGenre(FIND_GENRE));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindYear() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains("-" + FIND_YEAR + "-")) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findYear(FIND_YEAR));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testSearchArtist() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_ARTIST)) {
                testResults.add(song);
            }
        }

        MPDArtist artist = new MPDArtist(SEARCH_ARTIST);
        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchArtist(artist));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testSearchArtistString() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_ARTIST)) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchArtist(SEARCH_ARTIST));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testSearchAlbum() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_ALBUM)) {
                testResults.add(song);
            }
        }

        MPDAlbum album = new MPDAlbum(SEARCH_ALBUM);
        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchAlbum(album));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testSearchAlbumString() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_ALBUM)) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchAlbum(SEARCH_ALBUM));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testSearchTitleString() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_TITLE)) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchTitle(SEARCH_TITLE));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testSearchTitleStringSpace() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_TITLE_SPACE)) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchTitle(SEARCH_TITLE_SPACE));

        Assert.assertEquals(testResults.size(), foundSongs.size());
    }

    @Test
    public void testSearchAny() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(SEARCH_ANY)) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().searchAny(SEARCH_ANY));

        compareSongLists(testResults, foundSongs);
    }

    @Test
    public void testFindAny() throws MPDException {
        List<MPDSong> testResults = new ArrayList<>();

        for (MPDSong song : Songs.songs) {
            if (song.getFile().contains(FIND_ANY + "-")) {
                testResults.add(song);
            }
        }

        List<MPDSong> foundSongs = new ArrayList<>(getDatabase().findAny(FIND_ANY));

        compareSongLists(testResults, foundSongs);
    }

    private void compareDirs(File testFile, MPDFile file) throws Exception {
        List<File> testFiles = new ArrayList<>(Files.getTestFiles(testFile));
        List<MPDFile> files = new ArrayList<>(getDatabase().listDirectory(file));

        Assert.assertEquals(testFiles.size(), files.size());

        for (File f : testFiles) {
            boolean found = false;
            for (MPDFile mpdF : files) {
                if (f.getName().equals(mpdF.getName().replaceFirst(file.getName() + "/", ""))) {
                    found = true;
                    Assert.assertEquals(f.isDirectory(), mpdF.isDirectory());
                    if (f.isDirectory()) {
                        compareDirs(f, mpdF);
                    }
                }
            }

            Assert.assertTrue(found);
        }
    }

    private void compareSongs(MPDSong item1, MPDSong item2) {
        Assert.assertEquals(item1.getId(), item2.getId());
        Assert.assertEquals(item1.getName(), item2.getName());
        Assert.assertEquals(item1.getFile(), item2.getFile());
        Assert.assertEquals(item1.getAlbumName(), item2.getAlbumName());
        Assert.assertEquals(item1.getGenre(), item2.getGenre());
        Assert.assertEquals(item1.getGenre(), item2.getGenre());
        Assert.assertEquals(item1.getYear(), item2.getYear());
        Assert.assertEquals(item1.getTrack(), item2.getTrack());
        Assert.assertEquals(item1.getDiscNumber(), item2.getDiscNumber());
    }

    /**
     * @return the artistList
     */
    public static List<MPDArtist> getArtistList() {
        return artistList;
    }

    /**
     * @param aArtistList the artistList to set
     */
    public static void setArtistList(List<MPDArtist> aArtistList) {
        artistList = aArtistList;
    }

    /**
     * @return the albumList
     */
    public static List<MPDAlbum> getAlbumList() {
        return albumList;
    }

    /**
     * @param aAlbumList the albumList to set
     */
    public static void setAlbumList(List<MPDAlbum> aAlbumList) {
        albumList = aAlbumList;
    }

    /**
     * @return the genreList
     */
    public static List<MPDGenre> getGenreList() {
        return genreList;
    }

    /**
     * @param aGenreList the genreList to set
     */
    public static void setGenreList(List<MPDGenre> aGenreList) {
        genreList = aGenreList;
    }

    /**
     * @return the songList
     */
    public static List<MPDSong> getSongList() {
        return songList;
    }

    /**
     * @param aSongList the songList to set
     */
    public static void setSongList(List<MPDSong> aSongList) {
        songList = aSongList;
    }

    private void compareSongLists(List<MPDSong> testResults, List<MPDSong> foundSongs) {

        if (testResults.isEmpty()) {
            Assert.assertTrue("Bad test criteria.  Should have a size of at least 1", false);
        }

        assertEquals(testResults.size(), foundSongs.size());

        for (MPDSong song : testResults) {
            boolean found = false;
            for (MPDSong songDb : foundSongs) {
                if (song.getFile().equals(songDb.getFile())) {
                    found = true;
                    break;
                }
            }

            assertTrue(found);
        }
    }
}
