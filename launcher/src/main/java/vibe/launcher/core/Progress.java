package vibe.launcher.core;

/** Куда установщик сообщает, что он сейчас делает. */
public interface Progress {
    void status(String text);

    /** done/total, total <= 0 значит "неизвестно". */
    void progress(long done, long total);

    void log(String line);

    Progress NONE = new Progress() {
        @Override
        public void status(String text) {
        }

        @Override
        public void progress(long done, long total) {
        }

        @Override
        public void log(String line) {
        }
    };
}
